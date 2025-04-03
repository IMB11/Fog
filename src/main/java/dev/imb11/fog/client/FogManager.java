package dev.imb11.fog.client;

import dev.imb11.fog.api.CustomFogDefinition;
import dev.imb11.fog.api.FogColors;
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
		@NotNull var clientPlayerBiomeKeyOptional = clientWorld.getBiome(clientPlayer.getBlockPos()).getKey();
		if (clientPlayerBiomeKeyOptional.isEmpty()) {
			return;
		}

		CustomFogDefinition fogDefinition = FogRegistry.getFogDefinitionOrDefault(
				clientPlayerBiomeKeyOptional.get().getValue(), clientWorld);
		@Nullable FogColors colors = fogDefinition.colors();
		if (colors == null || FogConfig.getInstance().disableBiomeFogColour) {
			colors = FogColors.getDefault(clientWorld);
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
	 * Applies sunset color blending similar to vanilla Minecraft's implementation.
	 * Uses LCH color space for more perceptually correct transitions, with special handling
	 * for colors near white.
	 */
	private float[] applySunsetLogic(MinecraftClient client, float red, float green, float blue, float tickDelta) {
		if (!FogConfig.getInstance().disableSunsetFog && client.world != null) {
			float skyAngle = client.world.getSkyAngle(tickDelta);
			float blendSpeed = 0.0005F; // Speed of blending

			boolean isSunset = false;
			Vec3d sunColor;

			//? if >=1.21.2 {
			isSunset = client.world.getDimensionEffects().isSunRisingOrSetting(skyAngle);
			sunColor = Vec3d.unpackRgb(client.world.getDimensionEffects().getSkyColor(skyAngle));
			//?} else {
			/*// Because we have no good way of determining sunsets in non-overworld biomes before 1.21.2, we'll just apply this to the overworld.
			if (client.world.getDimensionEffects() instanceof DimensionEffects.Overworld) {
				float f = MathHelper.cos(skyAngle * 6.2831855F);
				isSunset = f >= -0.4F && f <= 0.4F;

				float t = MathHelper.cos(skyAngle * 6.2831855F);
				float g = t / 0.4F * 0.5F + 0.5F;
				float h = MathHelper.square(1.0F - (1.0F - MathHelper.sin(g * 3.1415927F)) * 0.99F);

				int color = ((MathHelper.floor(h * 255.0F) & 0xFF) << 24) |
						((MathHelper.floor((g * 0.3F + 0.7F) * 255.0F) & 0xFF) << 16) |
						((MathHelper.floor((g * g * 0.7F + 0.2F) * 255.0F) & 0xFF) << 8) |
						(MathHelper.floor(0.2F * 255.0F) & 0xFF);
				sunColor = Vec3d.unpackRgb(color);
			} else {
				sunColor = Vec3d.ZERO;
			}
			*///?}

			if (isSunset) {
				sunsetSunriseBlendFactor = Math.min(sunsetSunriseBlendFactor + (blendSpeed * tickDelta), 1.0F);
			} else {
				sunsetSunriseBlendFactor = Math.max(sunsetSunriseBlendFactor - (blendSpeed * tickDelta), 0.0F);
				}

			// Only apply sunset colors if we're actually in a sunset or have some blend factor
			if (sunsetSunriseBlendFactor > 0) {
				// Create color objects for our current fog color and the sunset color
				double[] rgbFog = new double[]{red, green, blue};
				double[] rgbSun = new double[]{sunColor.x, sunColor.y, sunColor.z};
				
				// Check if the fog color is close to white - special case
				if (isNearWhite(rgbFog)) {
					// For white-to-color transitions, we use a direct fade rather than going through 
					// the color wheel to prevent unwanted intermediates
					float[] result = directLerpForWhite(rgbFog, rgbSun, sunsetSunriseBlendFactor);
					return result;
				}
				
				// For regular colors, use LCH interpolation
				float[] result = lchColorLerp(rgbFog, rgbSun, sunsetSunriseBlendFactor);
				return result;
			}
		}

		return new float[]{red, green, blue};
	}
	
	/**
	 * Determines if a color is close to white
	 */
	private boolean isNearWhite(double[] rgb) {
		// Threshold for considering a color as "close to white"
		final double WHITE_THRESHOLD = 0.85;
		return rgb[0] > WHITE_THRESHOLD && rgb[1] > WHITE_THRESHOLD && rgb[2] > WHITE_THRESHOLD;
	}
	
	/**
	 * Special interpolation for transitions involving white
	 */
	private float[] directLerpForWhite(double[] rgb1, double[] rgb2, float factor) {
		// Use a smoother easing function for white transitions
		float easedFactor = (float) (0.5f - 0.5f * Math.cos(factor * Math.PI));
		
		float r = (float) MathHelper.lerp(easedFactor, rgb1[0], rgb2[0]);
		float g = (float) MathHelper.lerp(easedFactor, rgb1[1], rgb2[1]);
		float b = (float) MathHelper.lerp(easedFactor, rgb1[2], rgb2[2]);
		
		return new float[]{r, g, b};
	}
	
	/**
	 * Determines if a color is achromatic (very low chroma) in LCH space
	 */
	private boolean isAchromatic(double[] lch) {
		// Threshold for considering a color as "achromatic"
		final double ACHROMATIC_THRESHOLD = 5.0; // Adjust based on testing
		return lch[1] < ACHROMATIC_THRESHOLD; // lch[1] is the chroma component
	}
	
	/**
	 * Performs color interpolation in LCH color space for perceptually accurate blending
	 * LCH = Lightness, Chroma, Hue - a more perceptually uniform color space
	 * With special handling for achromatic colors
	 */
	private float[] lchColorLerp(double[] rgb1, double[] rgb2, float factor) {
		// Convert RGB to LCH
		double[] lch1 = rgbToLCH(rgb1);
		double[] lch2 = rgbToLCH(rgb2);
		
			// Special handling for achromatic colors (colors with very low chroma)
		boolean isFirstAchromatic = isAchromatic(lch1);
		boolean isSecondAchromatic = isAchromatic(lch2);
		
		// If one color is achromatic but not both, use the hue of the chromatic color
		if (isFirstAchromatic && !isSecondAchromatic) {
			// First color is achromatic, use the hue of the second color
			lch1[2] = lch2[2];
		} else if (!isFirstAchromatic && isSecondAchromatic) {
			// Second color is achromatic, use the hue of the first color
			lch2[2] = lch1[2];
		}
		// If both are achromatic, the hue doesn't matter for interpolation
		
		// Adjust hue for shortest path around the color wheel
		// Hue is in the range [0, 360)
		if (Math.abs(lch2[2] - lch1[2]) > 180) {
			if (lch1[2] < lch2[2]) {
				lch1[2] += 360;
			} else {
				lch2[2] += 360;
			}
		}
		
		// Use a smoother easing curve for the transition
		float easedFactor = (float) (0.5f - 0.5f * Math.cos(factor * Math.PI));
		
		// Interpolate in LCH space
		double l = MathHelper.lerp(easedFactor, lch1[0], lch2[0]);
		double c = MathHelper.lerp(easedFactor, lch1[1], lch2[1]);
		double h = MathHelper.lerp(easedFactor, lch1[2], lch2[2]) % 360.0;
		
		// Convert back to RGB
		double[] rgb = lchToRGB(new double[]{l, c, h});
		
		return new float[]{(float)rgb[0], (float)rgb[1], (float)rgb[2]};
	}
	
	/**
	 * Convert RGB to LCH color space
	 * RGB [0-1] -> LCH [0-100, 0-100+, 0-360]
	 */
	private double[] rgbToLCH(double[] rgb) {
		// RGB to XYZ
		double[] xyz = rgbToXYZ(rgb);
		// XYZ to Lab
		double[] lab = xyzToLab(xyz);
		// Lab to LCH
		return labToLCH(lab);
	}
	
	/**
	 * Convert RGB to XYZ color space
	 */
	private double[] rgbToXYZ(double[] rgb) {
		// Convert RGB to linear RGB
		double[] linear = new double[3];
		for (int i = 0; i < 3; i++) {
			if (rgb[i] <= 0.04045) {
				linear[i] = rgb[i] / 12.92;
			} else {
				linear[i] = Math.pow((rgb[i] + 0.055) / 1.055, 2.4);
			}
		}
		
		// Convert linear RGB to XYZ using sRGB/D65 matrix
		double x = linear[0] * 0.4124 + linear[1] * 0.3576 + linear[2] * 0.1805;
		double y = linear[0] * 0.2126 + linear[1] * 0.7152 + linear[2] * 0.0722;
		double z = linear[0] * 0.0193 + linear[1] * 0.1192 + linear[2] * 0.9505;
		
		return new double[]{x, y, z};
	}
	
	/**
	 * Convert XYZ to Lab color space
	 */
	private double[] xyzToLab(double[] xyz) {
		// D65 reference white point
		final double Xn = 0.95047;
		final double Yn = 1.0;
		final double Zn = 1.08883;
		
		double[] f = new double[3];
		f[0] = xyz[0] / Xn;
		f[1] = xyz[1] / Yn;
		f[2] = xyz[2] / Zn;
		
		for (int i = 0; i < 3; i++) {
			if (f[i] > 0.008856) {
				f[i] = Math.pow(f[i], 1.0 / 3.0);
			} else {
				f[i] = (f[i] * 7.787) + (16.0 / 116.0);
			}
		}
		
		double L = (116.0 * f[1]) - 16.0;
		double a = 500.0 * (f[0] - f[1]);
		double b = 200.0 * (f[1] - f[2]);
		
		return new double[]{L, a, b};
	}
	
	/**
	 * Convert Lab to LCH color space (polar form of Lab)
	 */
	private double[] labToLCH(double[] lab) {
		double C = Math.sqrt(lab[1] * lab[1] + lab[2] * lab[2]);
		
		double H = Math.toDegrees(Math.atan2(lab[2], lab[1]));
		if (H < 0) {
			H += 360.0;
		}
		
		return new double[]{lab[0], C, H}; // L, C, H
	}
	
	/**
	 * Convert LCH to RGB color space
	 * LCH [0-100, 0-100+, 0-360] -> RGB [0-1]
	 */
	private double[] lchToRGB(double[] lch) {
		// Convert LCH to Lab
		double[] lab = lchToLab(lch);
		// Convert Lab to XYZ
		double[] xyz = labToXYZ(lab);
		// Convert XYZ to RGB
		double[] rgb = xyzToRGB(xyz);
		
		// Clamp RGB values to [0, 1] range
		for (int i = 0; i < 3; i++) {
			rgb[i] = MathHelper.clamp(rgb[i], 0.0, 1.0);
		}
		
		return rgb;
	}
	
	/**
	 * Convert LCH to Lab color space
	 */
	private double[] lchToLab(double[] lch) {
		double a = lch[1] * Math.cos(Math.toRadians(lch[2]));
		double b = lch[1] * Math.sin(Math.toRadians(lch[2]));
		return new double[]{lch[0], a, b}; // L, a, b
	}
	
	/**
	 * Convert Lab to XYZ color space
	 */
	private double[] labToXYZ(double[] lab) {
		// D65 reference white point
		final double Xn = 0.95047;
		final double Yn = 1.0;
		final double Zn = 1.08883;
		
		double fy = (lab[0] + 16.0) / 116.0;
		double fx = fy + (lab[1] / 500.0);
		double fz = fy - (lab[2] / 200.0);
		
		double x, y, z;
		
		if (Math.pow(fy, 3) > 0.008856) {
			y = Math.pow(fy, 3);
		} else {
			y = (fy - 16.0 / 116.0) / 7.787;
		}
		
		if (Math.pow(fx, 3) > 0.008856) {
			x = Math.pow(fx, 3);
		} else {
			x = (fx - 16.0 / 116.0) / 7.787;
		}
		
		if (Math.pow(fz, 3) > 0.008856) {
			z = Math.pow(fz, 3);
		} else {
			z = (fz - 16.0 / 116.0) / 7.787;
		}
		
		return new double[]{x * Xn, y * Yn, z * Zn};
	}
	
	/**
	 * Convert XYZ to RGB color space
	 */
	private double[] xyzToRGB(double[] xyz) {
		// XYZ to linear RGB using sRGB/D65 matrix
		double lr = xyz[0] * 3.2406 + xyz[1] * -1.5372 + xyz[2] * -0.4986;
		double lg = xyz[0] * -0.9689 + xyz[1] * 1.8758 + xyz[2] * 0.0415;
		double lb = xyz[0] * 0.0557 + xyz[1] * -0.2040 + xyz[2] * 1.0570;
		
		// Linear RGB to sRGB
		double[] rgb = new double[3];
		double[] linear = new double[]{lr, lg, lb};
		
		for (int i = 0; i < 3; i++) {
			if (linear[i] <= 0.0031308) {
				rgb[i] = linear[i] * 12.92;
			} else {
				rgb[i] = 1.055 * Math.pow(linear[i], 1.0 / 2.4) - 0.055;
			}
		}
		
		return rgb;
	}

	public record FogSettings(double fogStart, double fogEnd, float fogRed, float fogGreen, float fogBlue) {}
}
