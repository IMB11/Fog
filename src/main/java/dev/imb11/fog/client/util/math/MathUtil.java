package dev.imb11.fog.client.util.math;

import net.minecraft.util.math.MathHelper;

public class MathUtil {
	public static int clamp(int value, int min, int max) {
		if (value < min) {
			value = min;
		} else if (value > max) {
			value = max;
		}

		return value;
	}

	public static float cube(float value) {
		return value * value * value;
	}

	public static int lerp(int start, int end, float t) {
		return Math.round(start * (1.0f - t) + (end * t));
	}

	public static float lerp(float start, float end, float t) {
		return start * (1.0f - t) + (end * t);
	}

	/**
	 * Maps a value between a range, from a starting minimum/maximum value to an ending maximum/maximum value.
	 *
	 * @param fromMin The starting minimum value.
	 * @param fromMax The starting maximum value.
	 * @param toMin   The ending minimum value.
	 * @param toMax   The ending maximum value.
	 * @param value   The value that should be mapped.
	 * @return The value, mapped between a range, from a starting minimum/maximum value to an ending maximum/maximum value.
	 */
	public static float mapRange(float fromMin, float fromMax, float toMin, float toMax, float value) {
		return toMin + (MathHelper.clamp(value, fromMin, fromMax) - fromMin) * (toMax - toMin) / (fromMax - fromMin);
	}
}
