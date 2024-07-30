package dev.imb11.fog.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.fog.client.util.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the colors of the fog during the day and night.
 */
public class FogColors {
	public static final FogColors DEFAULT = new FogColors("#add4ff", "#181f30");
	public static final FogColors DEFAULT_CAVE = new FogColors("#212121", "#101010");
	public static final Codec<FogColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("day").forGetter(FogColors::getDay),
			Codec.STRING.fieldOf("night").forGetter(FogColors::getNight)
	).apply(instance, FogColors::new));

	private final @NotNull String day;
	private final @NotNull String night;

	private transient @Nullable Color dayCached;
	private transient @Nullable Color nightCached;

	/**
	 * @param day The color of the fog during the day - in #RRGGBB format.
	 * @param night The color of the fog during the night - in #RRGGBB format.
	 */
	public FogColors(@NotNull String day, @NotNull String night) {
		assert day.matches("^#[0-9a-fA-F]{6}$") : "Invalid day color format: " + day;
		assert night.matches("^#[0-9a-fA-F]{6}$") : "Invalid night color format: " + night;
		this.day = day;
		this.night = night;
	}

	/**
	 * @return The color object of the fog during the day.
	 */
	public Color getDayColor() {
		if (dayCached == null) {
			dayCached = Color.parse(day);
		}

		return dayCached;
	}

	/**
	 * @return The color object of the fog during the night.
	 */
	public Color getNightColor() {
		if (nightCached == null) {
			nightCached = Color.parse(night);
		}

		return nightCached;
	}

	/**
	 * @return The color of the fog during the day - in #RRGGBB format.
	 */
	private @NotNull String getDay() {
		return day;
	}

	/**
	 * @return The color of the fog during the night - in #RRGGBB format.
	 */
	private @NotNull String getNight() {
		return night;
	}

	/**
	 * A builder for fog colors.
	 */
	public static class Builder {
		private @NotNull String day = DEFAULT.getDay();
		private @NotNull String night = DEFAULT.getNight();

		/**
		 * @param day The color of the fog during the day - in #RRGGBB format.
		 * @return The builder.
		 */
		public @NotNull Builder day(@NotNull String day) {
			this.day = day;
			return this;
		}

		/**
		 * @param night The color of the fog during the night - in #RRGGBB format.
		 * @return The builder.
		 */
		public @NotNull Builder night(@NotNull String night) {
			this.night = night;
			return this;
		}

		/**
		 * @return The fog colors built from the builder, or using the default values if not specified.
		 */
		public @NotNull FogColors build() {
			return new FogColors(day, night);
		}
	}
}
