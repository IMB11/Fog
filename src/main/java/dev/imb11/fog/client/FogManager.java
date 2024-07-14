package dev.imb11.fog.client;

import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.CustomFogDefinition;
import dev.imb11.fog.client.util.math.DarknessCalculation;
import dev.imb11.fog.client.util.math.InterpolatedValue;
import dev.imb11.fog.client.util.math.MathUtil;
import dev.imb11.fog.client.util.player.PlayerUtil;
import dev.imb11.fog.client.util.world.ClientWorldUtil;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FogManager {
	public static FogManager INSTANCE = new FogManager();
	public final InterpolatedValue raininess = new InterpolatedValue(0.0f, 0.03f);
	public final InterpolatedValue undergroundness = new InterpolatedValue(0.0f, 0.25f);
	public final InterpolatedValue fogStart = new InterpolatedValue(0.1f, 0.05f);
	public final InterpolatedValue fogEnd = new InterpolatedValue(0.85f, 0.05f);
	public final InterpolatedValue darkness = new InterpolatedValue(0.0f, 0.1f);
	public final InterpolatedValue fogColorRed = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
	public final InterpolatedValue fogColorGreen = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
	public final InterpolatedValue fogColorBlue = new InterpolatedValue((float) 0x33 / 255f, 0.05f);
	public final InterpolatedValue currentSkyLight = new InterpolatedValue(16.0F);
	public final InterpolatedValue currentBlockLight = new InterpolatedValue(16.0F);
	public final InterpolatedValue currentLight = new InterpolatedValue(16.0F);

	public FogManager() {
		@NotNull FogConfig config = FogConfig.getInstance();
		fogStart.resetTo(config.initialFogStart);
		fogEnd.resetTo(config.initialFogEnd);
	}

	public static @NotNull FogManager getInstance() {
		return INSTANCE;
	}

	public void onEndTick(@NotNull ClientWorld world) {
		@NotNull final var client = MinecraftClient.getInstance();
		@Nullable final var clientPlayer = client.player;
		if (clientPlayer == null) {
			return;
		}

		if (world.isRaining()) {
			raininess.interpolate(1.0f);
		} else {
			raininess.interpolate(0.0f);
		}

		@Nullable final BlockPos clientPlayerBlockPosition = clientPlayer.getBlockPos();
		if (clientPlayerBlockPosition == null) {
			return;
		}

		if (PlayerUtil.isPlayerAboveGround(
				clientPlayer.getEyePos().getY(), world.getSeaLevel(),
				world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, clientPlayerBlockPosition.getX(), clientPlayerBlockPosition.getZ())
		)) {
			this.undergroundness.interpolate(0.0F);
		} else {
			this.undergroundness.interpolate(1.0F);
		}

		float density = ClientWorldUtil.isFogDenseAtPosition(world, clientPlayerBlockPosition) ? 0.9F : 1.0F;
		// TODO: Apply the start and end multipliers in FogManager#getFogSettings
		DarknessCalculation darknessCalculation = DarknessCalculation.of(
				client, fogStart.getDefaultValue(), fogEnd.getDefaultValue() * density, client.getTickDelta());

		@NotNull var clientPlayerBiomeKeyOptional = world.getBiome(clientPlayer.getBlockPos()).getKey();
		if (clientPlayerBiomeKeyOptional.isEmpty()) {
			return;
		}

		@Nullable CustomFogDefinition.FogColors colors = FogRegistry.getBiomeFogDefinitionOrDefault(
				clientPlayerBiomeKeyOptional.get().getValue()).getColors();
		if (colors == null) {
			colors = FogRegistry.getDefaultBiomeColors();
		}

		long time = world.getTimeOfDay();
		boolean isDay = time < 12000;
		float blendFactor = isDay ? (time / 12000f) : ((time - 12000) / 12000f);
		float red = MathHelper.lerp(blendFactor, colors.getDayColor().red / 255f, colors.getNightColor().red / 255f);
		float green = MathHelper.lerp(blendFactor, colors.getDayColor().green / 255f, colors.getNightColor().green / 255f);
		float blue = MathHelper.lerp(blendFactor, colors.getDayColor().blue / 255f, colors.getNightColor().blue / 255f);
		this.fogColorRed.interpolate(red);
		this.fogColorGreen.interpolate(green);
		this.fogColorBlue.interpolate(blue);

		this.fogStart.interpolate(darknessCalculation.fogStart());
		this.fogEnd.interpolate(darknessCalculation.fogEnd());
		this.darkness.interpolate(darknessCalculation.darknessValue());

		this.currentSkyLight.interpolate(world.getLightLevel(LightType.SKY, clientPlayerBlockPosition));
		this.currentBlockLight.interpolate(world.getLightLevel(LightType.BLOCK, clientPlayerBlockPosition));
		this.currentLight.interpolate(world.getBaseLightLevel(clientPlayerBlockPosition, 0));
	}

	public float getUndergroundFactor(@NotNull MinecraftClient client, float deltaTicks) {
		@Nullable var clientCamera = client.cameraEntity;
		@Nullable var clientWorld = client.world;
		if (clientCamera == null || clientWorld == null) {
			return 0.0F;
		}

		float clientCameraYPosition = (float) clientCamera.getY();
		float seaLevel = clientWorld.getSeaLevel();
		// Map the client camera's Y position to a factor between 0 and 1 based on the sea level (+/- 32)
		float yFactor = MathHelper.clamp(MathUtil.mapRange(seaLevel - 32.0F, seaLevel + 32.0F, 1.0F, 0.0F, clientCameraYPosition), 0.0F, 1.0F);
		float undergroundnessValue = this.undergroundness.get(deltaTicks);
		float skyLight = this.currentSkyLight.get(deltaTicks);
		// Calculate the underground factor by lerping between yFactor, undergroundness, and sky light
		return MathHelper.lerp(yFactor, 1.0F - undergroundnessValue, skyLight / 16.0F);
	}

	public @NotNull FogSettings getFogSettings(float tickDelta, float viewDistance) {
		float fogStartValue = fogStart.get(tickDelta) * viewDistance;
		// Default to no multiplier
		float undergroundFogMultiplier = 1.0F;
		if (!FogConfig.getInstance().disableUndergroundFogMultiplier) {
			undergroundFogMultiplier = MathHelper.lerp(this.undergroundness.get(tickDelta), 0.75F, 1.0F);
			undergroundFogMultiplier = MathHelper.lerp(this.darkness.get(tickDelta), undergroundFogMultiplier, 1.0F);
		}

		float fogEndValue = viewDistance * (fogEnd.get(tickDelta));
		if (undergroundFogMultiplier > 0.78f) {
			fogEndValue /= 1 + undergroundFogMultiplier;
		}

		float fogRed = fogColorRed.get(tickDelta);
		float fogGreen = fogColorGreen.get(tickDelta);
		float fogBlue = fogColorBlue.get(tickDelta);

		float raininessValue = raininess.get(tickDelta);
		if (!FogConfig.getInstance().disableRaininessEffect && raininessValue > 0.0f) {
			fogEndValue /= 1.0f + 0.5f * raininessValue;

			// Darken the fog colour based on raininess
			fogRed *= 1f - raininessValue;
			fogGreen *= 1f - raininessValue;
			fogBlue *= 0.85f - raininessValue;
		}

		float darknessValue = this.darkness.get(tickDelta);
		fogRed *= 1 - darknessValue;
		fogGreen *= 1 - darknessValue;
		fogBlue *= 1 - darknessValue;

		return new FogSettings(fogStartValue, fogEndValue, fogRed, fogGreen, fogBlue);
	}

	public record FogSettings(double fogStart, double fogEnd, float fogRed, float fogGreen, float fogBlue) {}
}
