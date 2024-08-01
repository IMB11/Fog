package dev.imb11.fog.client.util.math;

public class CloudCalculator {
	public static float getCloudColor(long worldTime) {
		// Normalize the time within a day
		long timeOfDay = worldTime % 24000;

		// Define the color values
		float dayColor = 1.0f;
		float nightColor = 0.25f;

		float cloudColor;

		if (timeOfDay < 6000) {
			// Sunrise to noon: blend from nightColor to dayColor
			float t = (float)timeOfDay / 6000;
			cloudColor = MathUtil.lerp(nightColor, dayColor, t);
		} else if (timeOfDay < 18000) {
			// Noon to sunset: dayColor
			cloudColor = dayColor;
		} else {
			// Sunset to midnight: blend from dayColor to nightColor
			float t = (float)(timeOfDay - 18000) / 6000;
			cloudColor = MathUtil.lerp(dayColor, nightColor, t);
		}

		return cloudColor;
	}
}
