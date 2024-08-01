package dev.imb11.fog.client.util.color;

import dev.imb11.fog.client.util.math.MathUtil;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import static dev.imb11.fog.client.util.math.MathUtil.clamp;

/**
 * Color class that takes in red, green and blue values between 0-255. Values outside of this range are truncated.
 */
public class Color {
	public int red;
	public int green;
	public int blue;

	private static final int BIG_HEX_COLOR_CHARACTER_LENGTH = 6;
	private static final int SMALL_HEX_COLOR_CHARACTER_LENGTH = 3;

	public Color(int red, int green, int blue) {
		this.red = clamp(red, 0, 255);
		this.green = clamp(green, 0, 255);
		this.blue = clamp(blue, 0, 255);
	}

	public Color(int hex) {
		red = (hex >> 16) & 0xFF;
		green = (hex >> 8) & 0xFF;
		blue = hex & 0xFF;
	}

	public Color(@NotNull Color color) {
		red = color.red;
		green = color.green;
		blue = color.blue;
	}

	@Override
	public String toString() {
		return String.format("Color(%s, %s, %s)", red, green, blue);
	}

	/**
	 * Parses hexadecimal strings, in either {@code 0xfff} (the {@code 0x} prefix is required) or {@code #FFF} format (the {@code #} prefix is not required).
	 *
	 * @param hex A hexadecimal string, in either {@code 0xfff} (the {@code 0x} prefix is required) or {@code #FFF} format (the {@code #} prefix is not required).
	 * @return The parsed {@link Color}.
	 * @throws NumberFormatException Thrown when the {@code 0xfff} format was detected, but it couldn't be parsed by {@code Integer.parseInt(hex, 16)}.
	 */
	public static @NotNull Color parse(@NotNull String hex) throws NumberFormatException {
		hex = hex.trim();

		// Parse the 0xfff format
		if (hex.startsWith("0x")) {
			hex = hex.substring(2);
			int hexValue = Integer.parseInt(hex, 16);
			return new Color((hexValue >> 16) & 0xFF, (hexValue >> 8) & 0xFF, hexValue & 0xFF);
		}

		// Parse the #FFF format
		if (hex.startsWith("#")) {
			hex = hex.substring(1);
		}

		int red;
		int green;
		int blue;
		var hexLength = hex.length();
		if (hexLength == SMALL_HEX_COLOR_CHARACTER_LENGTH) {
			red = Integer.parseInt(hex.substring(0, 1), 16) * 255 / 16;
			green = Integer.parseInt(hex.substring(1, 2), 16) * 255 / 16;
			blue = Integer.parseInt(hex.substring(2, 3), 16) * 255 / 16;
		} else if (hexLength == BIG_HEX_COLOR_CHARACTER_LENGTH) {
			red = Integer.parseInt(hex.substring(0, 2), 16);
			green = Integer.parseInt(hex.substring(2, 4), 16);
			blue = Integer.parseInt(hex.substring(4, 6), 16);
		} else {
			throw new NumberFormatException(
					"Hex value {} was detected as the #FFF format, but it didn't have the correct amount of characters (3 or 6).");
		}

		return new Color(red, green, blue);
	}

	public int toInt() {
		int hex = red;
		hex = (hex << 8) + green;
		hex = (hex << 8) + blue;

		return hex;
	}

	public Color multiply(@NotNull Color color) {
		var newColor = new Color(this);

		newColor.red = clamp(newColor.red * color.red, 0, 255);
		newColor.green = clamp(newColor.green * color.green, 0, 255);
		newColor.blue = clamp(newColor.blue * color.blue, 0, 255);

		return newColor;
	}

	public Color add(@NotNull Color color) {
		var newColor = new Color(this);

		newColor.red = clamp(newColor.red + color.red, 0, 255);
		newColor.green = clamp(newColor.green + color.green, 0, 255);
		newColor.blue = clamp(newColor.blue + color.blue, 0, 255);

		return newColor;
	}

	public Color subtract(@NotNull Color color) {
		var newColor = new Color(this);

		newColor.red = clamp(newColor.red - color.red, 0, 255);
		newColor.green = clamp(newColor.green - color.green, 0, 255);
		newColor.blue = clamp(newColor.blue - color.blue, 0, 255);

		return newColor;
	}

	public Color invert() {
		var newColor = new Color(this);

		newColor.red = clamp(255 - this.red, 0, 255);
		newColor.green = clamp(255 - this.green, 0, 255);
		newColor.blue = clamp(255 - this.blue, 0, 255);

		return newColor;
	}

	public Color lerp(@NotNull Color endColor, float t) {
		var newColor = new Color(this);

		newColor.red = MathUtil.lerp(this.red, endColor.red, t);
		newColor.green = MathUtil.lerp(this.green, endColor.green, t);
		newColor.blue = MathUtil.lerp(this.blue, endColor.blue, t);

		return newColor;
	}
}
