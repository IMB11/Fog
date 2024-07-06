package dev.imb11.fog.client.util.math;

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
}
