package dev.imb11.fog.client;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.api.FogColors;
import dev.imb11.fog.client.compat.polytone.PolytoneCompat;
import dev.imb11.fog.client.registry.FogRegistry;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.client.util.math.DarknessCalculation;
import dev.imb11.fog.client.util.math.InterpolatedValue;
import dev.imb11.fog.client.util.math.MathUtil;
import dev.imb11.fog.client.util.player.PlayerUtil;
import dev.imb11.fog.client.util.world.ClientWorldUtil;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FogManager {
	public static FogManager INSTANCE = new FogManager();

	public final InterpolatedValue raininess;
	public final InterpolatedValue undergroundness;
	public final InterpolatedValue fogStart;
	public final InterpolatedValue fogEnd;
	public final InterpolatedValue darkness;
	public final InterpolatedValue fogColorRed;
	public final InterpolatedValue fogColorGreen;
	public final InterpolatedValue fogColorBlue;
	public final InterpolatedValue currentSkyLight;
	public final InterpolatedValue currentBlockLight;
	public final InterpolatedValue currentLight;
	public final InterpolatedValue currentStartMultiplier;
	public final InterpolatedValue currentEndMultiplier;

	public boolean hasSetup = false;
	public float sunsetSunriseBlendFactor = 0.0F;

	public FogManager() {
		@NotNull FogConfig config = FogConfig.getInstance();

		raininess = new InterpolatedValue(0.0f, config.raininessTransitionSpeed);
		undergroundness = new InterpolatedValue(0.0f, config.undergroundnessTransitionSpeed);
		fogStart = new InterpolatedValue(config.initialFogStart, config.fogStartTransitionSpeed);
		fogEnd = new InterpolatedValue(config.initialFogEnd, config.fogEndTransitionSpeed);
		darkness = new InterpolatedValue(0.0f, config.darknessTransitionSpeed);
		fogColorRed = new InterpolatedValue(-1.0f, config.fogColorTransitionSpeed);
		fogColorGreen = new InterpolatedValue(-1.0f, config.fogColorTransitionSpeed);
		fogColorBlue = new InterpolatedValue(-1.0f, config.fogColorTransitionSpeed);
		currentSkyLight = new InterpolatedValue(16.0F);
		currentBlockLight = new InterpolatedValue(16.0F);
		currentLight = new InterpolatedValue(16.0F);
		currentStartMultiplier = new InterpolatedValue(1.0F, config.startMultiplierTransitionSpeed);
		currentEndMultiplier = new InterpolatedValue(1.0F, config.endMultiplierTransitionSpeed);

		fogStart.resetTo(config.initialFogStart);
		fogEnd.resetTo(config.initialFogEnd);
	}

	public static @NotNull FogManager getInstance() {
		return INSTANCE;
	}

	public static boolean isInDisabledBiome() {
		MinecraftClient client = MinecraftClient.getInstance();
		RegistryEntry<Biome> biome = client.world.getBiomeAccess().getBiome(client.player.getBlockPos());

		return FogConfig.getInstance().disabledBiomes.contains(biome.getIdAsString());
	}

	private static float getBlendFactor(@NotNull ClientWorld world) {
		long time = world.getTimeOfDay() % 24000;
		float blendFactor;
		if (time < 11000) {
			// Daytime
			blendFactor = 1.0f;
		} else if (time < 13000) {
			// Blend from day to night
			blendFactor = MathUtil.lerp(1.0f, 0.0f, (time - 11000) / 2000f);
		} else if (time < 22000) {
			// Nighttime
			blendFactor = 0.0f;
		} else if (time < 23000) {
			// Blend from night to day
			blendFactor = MathUtil.lerp(0.0f, 1.0f, (time - 22000) / 1000f);
		} else {
			// Constant day from 23000 to 24000 ticks
			blendFactor = 1.0f;
		}
		return blendFactor;
	}

	public void onEndTick(@NotNull ClientWorld clientWorld) {
		@NotNull final var client = MinecraftClient.getInstance();
		@Nullable final var clientPlayer = client.player;
		if (clientPlayer == null) {
			return;
		}

		@Nullable final BlockPos clientPlayerBlockPosition = clientPlayer.getBlockPos();
		if (clientPlayerBlockPosition == null) {
			return;
		}

		var isClientPlayerAboveGround = PlayerUtil.isPlayerAboveGround(clientPlayer);
		if (isClientPlayerAboveGround) {
			this.undergroundness.interpolate(0.0F);
		} else {
			this.undergroundness.interpolate(1.0F);
		}

		if (isClientPlayerAboveGround && clientWorld.getBiome(
				clientPlayer.getBlockPos()).value().hasPrecipitation() && clientWorld.isRaining()) {
			raininess.interpolate(1.0f);
		} else {
			raininess.interpolate(0.0f);
		}

		float density = ClientWorldUtil.isFogDenseAtPosition(clientWorld, clientPlayerBlockPosition) ? 0.9F : 1.0F;
		float tickDelta = client.getRenderTickCounter().getTickDelta(true);

		DarknessCalculation darknessCalculation = DarknessCalculation.of(
				client, fogStart.getDefaultValue(), fogEnd.getDefaultValue() * density, tickDelta);
		var biomeKey =  clientWorld.getBiome(clientPlayer.getBlockPos());
		@NotNull var clientPlayerBiomeKeyOptional = biomeKey.getKey();
		if (clientPlayerBiomeKeyOptional.isEmpty()) {
			return;
		}

		CustomFogDefinition fogDefinition = FogRegistry.getFogDefinitionOrDefault(
				clientPlayerBiomeKeyOptional.get().getValue(), clientWorld);
		@Nullable FogColors colors = fogDefinition.colors();
		if (colors == null || FogConfig.getInstance().disableBiomeFogColour) {
			colors = FogColors.getDefault(clientWorld);
		}
		if (PolytoneCompat.shouldUsePolytone()) {
			colors = PolytoneCompat.getFogColorsFromPolytone(clientPlayerBiomeKeyOptional.get(), colors);
		}

		float blendFactor = getBlendFactor(clientWorld);
		Color finalNightColor = getFinalNightColor(clientWorld, colors);

		// Base day/night color blending
		float red = MathHelper.lerp(blendFactor, finalNightColor.red / 255f, colors.getDayColor().red / 255f);
		float green = MathHelper.lerp(blendFactor, finalNightColor.green / 255f, colors.getDayColor().green / 255f);
		float blue = MathHelper.lerp(blendFactor, finalNightColor.blue / 255f, colors.getDayColor().blue / 255f);

		if (!hasSetup) {
			this.fogColorRed.set(red);
			this.fogColorGreen.set(green);
			this.fogColorBlue.set(blue);
			hasSetup = true;
		} else {
			this.fogColorRed.interpolate(red);
			this.fogColorGreen.interpolate(green);
			this.fogColorBlue.interpolate(blue);
		}

		this.currentStartMultiplier.interpolate(fogDefinition.startMultiplier());
		this.currentEndMultiplier.interpolate(fogDefinition.endMultiplier());

		this.fogStart.interpolate(darknessCalculation.fogStart());
		this.fogEnd.interpolate(darknessCalculation.fogEnd());
		this.darkness.interpolate(darknessCalculation.darknessValue());

		this.currentSkyLight.interpolate(clientWorld.getLightLevel(LightType.SKY, clientPlayerBlockPosition));
		this.currentBlockLight.interpolate(clientWorld.getLightLevel(LightType.BLOCK, clientPlayerBlockPosition));
		this.currentLight.interpolate(clientWorld.getBaseLightLevel(clientPlayerBlockPosition, 0));
	}

	private Color getFinalNightColor(@NotNull ClientWorld world, FogColors fogColors) {
		Color newMoonColor = Color.from(FogConfig.getInstance().newMoonColor);
		if (!FogConfig.getInstance().disableMoonPhaseColorTransition) {
			float blendFactor = switch (world.getMoonPhase()) {
				case 0 -> 0.0f;  // new moon
				case 1, 7 -> 0.25f; // 1/4 moon
				case 2, 6 -> 0.5f;  // 1/2 moon
				case 3, 5 -> 0.75f; // 3/4 moon
				case 4 -> 1.0f;  // full moon
				default -> 1.0f;
			};
			return fogColors.getNightColor().lerp(newMoonColor, blendFactor);
		} else {
			return fogColors.getNightColor();
		}
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
		MinecraftClient client = MinecraftClient.getInstance();

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
			fogEndValue /= 1.0f + raininessValue;

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

		// Sunset
		float[] sunsetAdjustedColors = applySunsetLogic(client, fogRed, fogGreen, fogBlue, tickDelta);
		fogRed = sunsetAdjustedColors[0];
		fogGreen = sunsetAdjustedColors[1];
		fogBlue = sunsetAdjustedColors[2];

		return new FogSettings(fogStartValue, fogEndValue, fogRed, fogGreen, fogBlue);
	}

	/**
	 * Applies sunset/sunrise color blending to fog colors.
	 * Uses vanilla Minecraft's sunset detection for accurate timing.
	 */
	private float[] applySunsetLogic(MinecraftClient client, float red, float green, float blue, float tickDelta) {
		if (!FogConfig.getInstance().disableSunsetFog && client.world != null) {
			float skyAngle = client.world.getSkyAngle(tickDelta);
			
			boolean isSunset = false;
			Vec3d sunColor;

			//? if >=1.21.2 {
			isSunset = client.world.getDimensionEffects().isSunRisingOrSetting(skyAngle);
			sunColor = Vec3d.unpackRgb(client.world.getDimensionEffects().getSkyColor(skyAngle));
			//?} else {
			/*// For versions before 1.21.2, use a simple approach for the overworld
			if (client.world.getDimensionEffects() instanceof DimensionEffects.Overworld) {
				float cosAngle = MathHelper.cos(skyAngle * 6.2831855F);
				isSunset = cosAngle >= -0.4F && cosAngle <= 0.4F;
				
				// Approximate vanilla sunset color
				float g = Math.max(cosAngle, 0.0F) / 0.4F * 0.5F + 0.5F;
				float h = 1.0F - (1.0F - MathHelper.sin(g * 3.1415927F)) * 0.99F;
				h = h * h;
				
				int color = ((MathHelper.floor(h * 255.0F) & 0xFF) << 24) |
						((MathHelper.floor((g * 0.3F + 0.7F) * 255.0F) & 0xFF) << 16) |
						((MathHelper.floor((g * g * 0.7F + 0.2F) * 255.0F) & 0xFF) << 8) |
						(MathHelper.floor(0.2F * 255.0F) & 0xFF);
				sunColor = Vec3d.unpackRgb(color);
			} else {
				isSunset = false;
				sunColor = Vec3d.ZERO;
			}
			*///?}

			// Only apply sunset colors if we're actually in sunrise/sunset
			if (isSunset) {
				// Calculate blend intensity based on sun angle
				// This creates a bell curve that peaks during actual sunrise/sunset
				// and falls off quickly as the sun moves away from the horizon
				float blendIntensity;
				float cosAngle = MathHelper.cos(skyAngle * 6.2831855F);
				
				// Create a curve that peaks at 1.0 when cosAngle is 0 (sun at horizon)
				// and drops off to 0.0 when cosAngle approaches -0.4 or 0.4
				blendIntensity = 1.0F - Math.abs(cosAngle) / 0.4F;
				blendIntensity = MathHelper.clamp(blendIntensity * blendIntensity * (3 - 2 * blendIntensity), 0.0F, 1.0F);
				
				// Apply a smooth fade based on the blend intensity
				if (blendIntensity > 0) {
					// Simple direct RGB blending for sunset colors
					// This is simpler and more predictable than the complex LCH color space conversions
					float sunR = (float) sunColor.x;
					float sunG = (float) sunColor.y;
					float sunB = (float) sunColor.z;
					
					// Apply sunset colors with greater intensity during actual sunset
					red = MathHelper.lerp(blendIntensity * 0.7F, red, sunR);
					green = MathHelper.lerp(blendIntensity * 0.7F, green, sunG);
					blue = MathHelper.lerp(blendIntensity * 0.7F, blue, sunB);
				}
			}
		}

		return new float[]{red, green, blue};
	}

	public record FogSettings(double fogStart, double fogEnd, float fogRed, float fogGreen, float fogBlue) {}
}
