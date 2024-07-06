package dev.imb11.fog.client;

import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.resource.CustomFogDefinition;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.client.util.math.DarknessCalculation;
import dev.imb11.fog.client.util.math.InterpolatedValue;
import dev.imb11.fog.client.util.player.PlayerUtil;
import dev.imb11.fog.client.util.world.ClientWorldUtil;
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

	public static @NotNull FogManager getInstance() {
		return INSTANCE;
	}

	public boolean shouldApplyHaze(@NotNull ClientWorld clientWorld, float deltaTick) {
		return this.undergroundness.get(deltaTick) <= 0.25f;
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
				world.getTopY(Heightmap.Type.WORLD_SURFACE, clientPlayerBlockPosition.getX(), clientPlayerBlockPosition.getZ())
		)) {
			this.undergroundness.interpolate(0.0F, 0.05f);
		} else {
			this.undergroundness.interpolate(1.0F);
		}

		float density = ClientWorldUtil.isFogDenseAtPosition(world, clientPlayerBlockPosition) ? 0.9F : 1.0F;

		DarknessCalculation darknessCalculation = DarknessCalculation.of(
				client, fogStart.getDefaultValue(), fogEnd.getDefaultValue() * density, client.getTickDelta());

		CustomFogDefinition.FogColors defaultColourEntry = FogRegistry.getDefaultBiomeColors();
		CustomFogDefinition biomeColourEntry = FogRegistry.getBiomeOrDefault(
				world.getBiome(clientPlayer.getBlockPos()).getKey().get().getValue());

		// TODO: Should interpolate between the day and night colours.
		if(biomeColourEntry.getColors().isPresent()) {
			CustomFogDefinition.FogColors colors = biomeColourEntry.getColors().get();
			Color color = world.isNight() ? colors.getNightColor() : colors.getDayColor();
			this.fogColorBlue.interpolate(color.red / 255f);
			this.fogColorGreen.interpolate(color.green / 255f);
			this.fogColorRed.interpolate(color.blue / 255f);
		} else {
			Color color = world.isNight() ? defaultColourEntry.getNightColor() : defaultColourEntry.getDayColor();

			this.fogColorBlue.interpolate(color.red / 255f);
			this.fogColorGreen.interpolate(color.green / 255f);
			this.fogColorRed.interpolate(color.blue / 255f);
		}

		this.fogStart.interpolate(darknessCalculation.fogStart());
		this.fogEnd.interpolate(darknessCalculation.fogEnd());
		this.darkness.interpolate(darknessCalculation.darknessValue());

		this.currentSkyLight.interpolate(world.getLightLevel(LightType.SKY, clientPlayerBlockPosition));
		this.currentBlockLight.interpolate(world.getLightLevel(LightType.BLOCK, clientPlayerBlockPosition));
		this.currentLight.interpolate(world.getBaseLightLevel(clientPlayerBlockPosition, 0));
	}

	public static float mapRange(float fromMin, float fromMax, float toMin, float toMax, float value) {
		float clampedValue = MathHelper.clamp(value, fromMin, fromMax);
		return toMin + (clampedValue - fromMin) * (toMax - toMin) / (fromMax - fromMin);
	}

	public float getUndergroundFactor(@NotNull MinecraftClient client, float deltaTicks) {
		@Nullable var clientCamera = client.cameraEntity;
		@Nullable var clientWorld = client.world;
		if (clientCamera == null || clientWorld == null) {
			return 0.0F;
		}

		float y = (float) clientCamera.getY();
		float seaLevel = clientWorld.getSeaLevel();

		// Map y to a factor between 0 and 1 based on sea level +/- 32
		float yFactor = mapRange(seaLevel - 32.0F, seaLevel + 32.0F, 1.0F, 0.0F, y);
		yFactor = MathHelper.clamp(yFactor, 0.0F, 1.0F); // Clamp yFactor to ensure it's within [0, 1]

		float undergroundnessValue = this.undergroundness.get(deltaTicks);
		float skyLight = this.currentSkyLight.get(deltaTicks);

		// Calculate underground factor by lerping between yFactor, undergroundness, and sky light
		return MathHelper.lerp(yFactor, 1.0F - undergroundnessValue, skyLight / 16.0F);
	}

	public FogSettings getFogSettings(float tickDelta, float viewDistance) {
		float fogStartValue = fogStart.get(tickDelta) * viewDistance;

		// Calculate undergroundness fog multiplier
		// TODO: Add a config option for disabling the undergroundness fog multiplier
		float undergroundFogMultiplier = MathHelper.lerp(this.undergroundness.get(tickDelta), 0.75F, 1.0F);
		undergroundFogMultiplier = MathHelper.lerp(this.darkness.get(tickDelta), undergroundFogMultiplier, 1.0F);

		// Calculate fog end considering raininess and underground fog multiplier
		float raininessValue = raininess.get(tickDelta);
		float fogEndValue = viewDistance * (fogEnd.get(tickDelta)) * undergroundFogMultiplier;

		float fogRed = fogColorRed.get(tickDelta);
		float fogGreen = fogColorGreen.get(tickDelta);
		float fogBlue = fogColorBlue.get(tickDelta);

		// Adjust fog end based on raininess
		if (raininessValue > 0.0f) {
			fogEndValue /= 1.0f + 0.5f * raininessValue;

			// Darken fog colour based on raininess
			fogRed *= 1f - raininessValue;
			fogGreen *= 1f - raininessValue;
			fogBlue *= 0.85f - raininessValue;
		}

		// Adjust fog color based on darkness
		float darknessValue = this.darkness.get(tickDelta);

		fogRed *= 1 - darknessValue;
		fogGreen *= 1 - darknessValue;
		fogBlue *= 1 - darknessValue;

		return new FogSettings(fogStartValue, fogEndValue, fogRed, fogGreen, fogBlue);
	}

	public record FogSettings(double fogStart, double fogEnd, float fogR, float fogG, float fogB) {}
}
