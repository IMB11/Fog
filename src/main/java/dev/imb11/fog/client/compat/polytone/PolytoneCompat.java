package dev.imb11.fog.client.compat.polytone;

import dev.imb11.fog.api.FogColors;
import dev.imb11.fog.config.FogConfig;
import dev.imb11.fog.mixin.compat.BiomeEffectsManagerAccessor;
import dev.imb11.mru.LoaderUtils;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.mehvahdjukaar.polytone.Polytone;

import java.util.HashMap;
import java.util.Map;

public class PolytoneCompat {
	public static boolean shouldUsePolytone() {
		return LoaderUtils.isModInstalled("polytone") && FogConfig.getInstance().prioritizePolytoneFogColors;
	}

	private static final Map<String, Integer> darkenCache = new HashMap<>();
	private static int darkenColor(int colorInt, double factor) {
		if (factor < 0 || factor > 1) {
			throw new IllegalArgumentException("Darkening factor must be between 0 and 1.");
		}

		String key = colorInt + "-" + factor;
		if (darkenCache.containsKey(key)) {
			return darkenCache.get(key);
		}

		int red = (colorInt >> 16) & 0xFF;
		int green = (colorInt >> 8) & 0xFF;
		int blue = colorInt & 0xFF;

		red = Math.max(0, (int) (red * (1 - factor)));
		green = Math.max(0, (int) (green * (1 - factor)));
		blue = Math.max(0, (int) (blue * (1 - factor)));

		int darkenedColor = (red << 16) | (green << 8) | blue;
		darkenCache.put(key, darkenedColor);
		return darkenedColor;
	}

	private static String intToRgbHex(int colorInt) {
		return String.format("#%06X", colorInt);
	}

	public static FogColors getFogColorsFromPolytone(RegistryKey<Biome> biome, FogColors defaultColors) {
		var effects = ((BiomeEffectsManagerAccessor) Polytone.BIOME_MODIFIERS).getEffectsToApply().get(biome);
		if (effects == null) return defaultColors;
		if (effects.fogColor().isEmpty()) return defaultColors;

		int dayColor = effects.fogColor().get();
		int nightColor = darkenColor(dayColor, 0.3);
		return new FogColors(intToRgbHex(dayColor), intToRgbHex(nightColor));
	}
}
