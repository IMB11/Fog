package dev.imb11.fog.client.util.math;

public class CloudCalculator {
	// TODO: Constants into FogConfig and make the relevant mixin toggleable.
	public static float getCloudColor(long worldTime) {
		// Normalize the time within a day
		long timeOfDay = worldTime % 24000;

		// Define the color values
		float dayColor = 1.0f;
		float nightColor = 0.25f;

		float cloudColor;

		if (timeOfDay < 10000) {
			// Daytime
			cloudColor = dayColor;
		} else if (timeOfDay < 11000) {
			// Transition starts just after 5:00 PM but still dayColor
			cloudColor = dayColor;
		} else if (timeOfDay < 13000) {
			// Blend from dayColor to nightColor
			float t = (float)(timeOfDay - 11000) / 2000;
			cloudColor = MathUtil.lerp(dayColor, nightColor, t);
		} else if (timeOfDay < 22000) {
			// Nighttime
			cloudColor = nightColor;
		} else if (timeOfDay < 23000) {
			// Blend from nightColor to dayColor
			float t = (float)(timeOfDay - 22000) / 1000;
			cloudColor = MathUtil.lerp(nightColor, dayColor, t);
		} else {
			// Constant dayColor from 23000 to 24000 ticks
			cloudColor = dayColor;
		}


		return cloudColor;
	}
}
