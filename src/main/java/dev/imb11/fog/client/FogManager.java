package dev.imb11.fog.client;

import dev.imb11.fog.api.FogColors;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.api.CustomFogDefinition;
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
	public final InterpolatedValue currentStartMultiplier = new InterpolatedValue(1.0F);
	public final InterpolatedValue currentEndMultiplier = new InterpolatedValue(1.0F);

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

		if (world.hasRain(clientPlayer.getBlockPos()) && world.getBiome(clientPlayer.getBlockPos()).value().hasPrecipitation()) {
			raininess.interpolate(1.0f);
		} else {
			raininess.interpolate(0.0f, 1f);
		}

		@Nullable final BlockPos clientPlayerBlockPosition = clientPlayer.getBlockPos();
		if (clientPlayerBlockPosition == null) {
			return;
		}

		if (PlayerUtil.isPlayerAboveGround(clientPlayer)) {
			this.undergroundness.interpolate(0.0F);
		} else {
			this.undergroundness.interpolate(1.0F);
		}

		float density = ClientWorldUtil.isFogDenseAtPosition(world, clientPlayerBlockPosition) ? 0.9F : 1.0F;

		/*? if <1.21 {*/
		/*float tickDelta = client.getTickDelta();
		*//*?} else {*/
		float tickDelta = client.getRenderTickCounter().getTickDelta(true);
		/*?}*/

		// TODO: Apply the start and end multipliers in FogManager#getFogSettings
		DarknessCalculation darknessCalculation = DarknessCalculation.of(
				client, fogStart.getDefaultValue(), fogEnd.getDefaultValue() * density, tickDelta);

		@NotNull var clientPlayerBiomeKeyOptional = world.getBiome(clientPlayer.getBlockPos()).getKey();
		if (clientPlayerBiomeKeyOptional.isEmpty()) {
			return;
		}

		CustomFogDefinition fogDefinition = FogRegistry.getFogDefinitionOrDefault(clientPlayerBiomeKeyOptional.get().getValue(), world);
		@Nullable FogColors colors = fogDefinition.colors();
		if (colors == null || FogConfig.getInstance().disableBiomeFogColour) {
			colors = FogColors.DEFAULT;
		}

		float blendFactor = getBlendFactor(world);
		float red = MathHelper.lerp(blendFactor, colors.getNightColor().red / 255f, colors.getDayColor().red / 255f);
		float green = MathHelper.lerp(blendFactor, colors.getNightColor().green / 255f, colors.getDayColor().green / 255f);
		float blue = MathHelper.lerp(blendFactor, colors.getNightColor().blue / 255f, colors.getDayColor().blue / 255f);
		this.fogColorRed.interpolate(red);
		this.fogColorGreen.interpolate(green);
		this.fogColorBlue.interpolate(blue);

		this.currentStartMultiplier.interpolate(fogDefinition.startMultiplier());
		this.currentEndMultiplier.interpolate(fogDefinition.endMultiplier());

		this.fogStart.interpolate(darknessCalculation.fogStart());
		this.fogEnd.interpolate(darknessCalculation.fogEnd());
		this.darkness.interpolate(darknessCalculation.darknessValue());

		this.currentSkyLight.interpolate(world.getLightLevel(LightType.SKY, clientPlayerBlockPosition));
		this.currentBlockLight.interpolate(world.getLightLevel(LightType.BLOCK, clientPlayerBlockPosition));
		this.currentLight.interpolate(world.getBaseLightLevel(clientPlayerBlockPosition, 0));
	}

	private static float getBlendFactor(@NotNull ClientWorld world) {
		long time = world.getTimeOfDay() % 24000;
		float blendFactor;
		if (time < 11000) {
			blendFactor = 1.0f; // Daytime
		} else if (time < 13000) {
			blendFactor = MathUtil.lerp(1.0f, 0.0f, (time - 11000) / 2000f); // Blend from day to night
		} else if (time < 22000) {
			blendFactor = 0.0f; // Nighttime
		} else if (time < 23000) {
			blendFactor = MathUtil.lerp(0.0f, 1.0f, (time - 22000) / 1000f); // Blend from night to day
		} else {
			blendFactor = 1.0f; // Constant day from 23000 to 24000 ticks
		}
		return blendFactor;
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
		float yFactor = MathHelper.clamp(
				MathUtil.mapRange(seaLevel - 32.0F, seaLevel + 32.0F, 1.0F, 0.0F, clientCameraYPosition), 0.0F, 1.0F);
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
			undergroundFogMultiplier = this.undergroundness.get(tickDelta);
			undergroundFogMultiplier = MathHelper.lerp(this.darkness.get(tickDelta), undergroundFogMultiplier, 1.0F);
		}

		float fogEndValue = viewDistance * (fogEnd.get(tickDelta));
		if (undergroundFogMultiplier > 0.0F) {
			fogEndValue /= 1 + undergroundFogMultiplier;
			fogStartValue *= Math.max(0.1f, 0.5f - undergroundFogMultiplier);
		}

		float fogRed = fogColorRed.get(tickDelta);
		float fogGreen = fogColorGreen.get(tickDelta);
		float fogBlue = fogColorBlue.get(tickDelta);

		float raininessValue = raininess.get(tickDelta);

		if (!FogConfig.getInstance().disableRaininessEffect && raininessValue > 0.0f) {
			fogEndValue /= 1.0f + 0.5f * raininessValue;

			// Darken the fog colour based on raininess
			fogRed = Math.max(0.1f, fogRed - (0.5f * raininessValue));
			fogGreen = Math.max(0.1f, fogGreen - (0.5f * raininessValue));
			fogBlue = Math.max(0.1f, fogBlue - (0.5f * raininessValue));
		}

		float darknessValue = this.darkness.get(tickDelta);
		fogRed *= 1 - darknessValue;
		fogGreen *= 1 - darknessValue;
		fogBlue *= 1 - darknessValue;

		fogStartValue *= this.currentStartMultiplier.get(tickDelta);
		fogEndValue *= this.currentEndMultiplier.get(tickDelta);

		return new FogSettings(fogStartValue, fogEndValue, fogRed, fogGreen, fogBlue);
	}

	public record FogSettings(double fogStart, double fogEnd, float fogRed, float fogGreen, float fogBlue) {}
}
