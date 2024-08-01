package dev.imb11.fog.client.util.math;

public class CloudCalculator {
	// TODO: Constants into FogConfig and make the relevant mixin toggleable.
	public static float getCloudColor(long worldTime) {
		// Define the color values
		float dayColor = 1.0f;
		float nightColor = 0.25f;

		float cloudColor;

		if (worldTime < 10000) {
			// Daytime
			cloudColor = dayColor;
		} else if (worldTime < 11000) {
			// Transition starts just after 5:00 PM but still dayColor
			cloudColor = dayColor;
		} else if (worldTime < 13000) {
			// Blend from dayColor to nightColor
			float t = (float)(worldTime - 11000) / 2000;
			cloudColor = MathUtil.lerp(dayColor, nightColor, t);
		} else if (worldTime < 22000) {
			// Nighttime
			cloudColor = nightColor;
		} else if (worldTime < 23000) {
			// Blend from nightColor to dayColor
			float t = (float)(worldTime - 22000) / 1000;
			cloudColor = MathUtil.lerp(nightColor, dayColor, t);
		} else {
			// Constant dayColor from 23000 to 24000 ticks
			cloudColor = dayColor;
		}


		return cloudColor;
	}
}
