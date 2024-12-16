package dev.imb11.fog.api;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.fog.client.util.color.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the colors of the fog during the day and night.
 */
public class FogColors {
	// TODO: De-hardcode these default values, default should also use the sky's color
	public static final String DEFAULT_SURFACE_DAY_COLOR = "#b9d2fd";
	public static final String DEFAULT_SURFACE_NIGHT_COLOR = "#000000";
	public static final FogColors DEFAULT = new FogColors(DEFAULT_SURFACE_DAY_COLOR, DEFAULT_SURFACE_NIGHT_COLOR, null);
	public static final FogColors DEFAULT_CAVE = new FogColors("#212121", "#101010", null);
	public static final Codec<FogColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("day").forGetter(FogColors::getDay),
			Codec.either(Codec.STRING, NightColors.CODEC).fieldOf("night").forGetter(FogColors::getNight)
	).apply(instance, FogColors::new));

	private final @NotNull String day;
	private final @Nullable Either<String, NightColors> night;

	private transient @Nullable Color dayCached;
	private transient @Nullable Color nightFullMoonCached;
	private transient @Nullable Color nightNewMoonCached;

	/**
	 * This constructor is only for codec serialization usage
	 * @param day The color of the fog during the day - in #RRGGBB format.
	 * @param night The color of the fog during the night - in #RRGGBB format or an object defining both nightFullMoon and nightNewMoon color in #RRGGBB format.
	 */
	private FogColors(@NotNull String day, @NotNull Either<String, NightColors> night) {
		AssertColorMatchesFormat(day, "day");
		this.day = day;
		night.ifLeft(nightOldFormat -> AssertColorMatchesFormat(nightOldFormat, "night"));
		night.ifRight(nightNewFormat -> {
			AssertColorMatchesFormat(nightNewFormat.nightFullMoon(), "nightFullMoon");
			AssertColorMatchesFormat(nightNewFormat.nightNewMoon(), "nightNewMoon");
		});
		this.night = night;
	}

	/**
	 * @param day The color of the fog during the day - in #RRGGBB format.
	 * @param nightFullMoon The color of the fog during the night - in #RRGGBB format.
	 * @param nightNewMoon The color of the fog during the night at full moon - in #RRGGBB format or null.
	 */
	public FogColors(@NotNull String day, @NotNull String nightFullMoon, @Nullable String nightNewMoon) {
		AssertColorMatchesFormat(day, "day");
		AssertColorMatchesFormat(nightFullMoon, "nightFullMoon");
		if (nightNewMoon != null) {
			AssertColorMatchesFormat(nightNewMoon, "nightNewMoon");
			this.nightNewMoonCached = Color.parse(nightNewMoon);
		}
		this.nightFullMoonCached = Color.parse(nightFullMoon);
		this.day = day;
		this.night = null; // This field is only used when initialized via serialization
	}

	public static void AssertColorMatchesFormat(String color, String field) {
		assert color.matches("^#[0-9a-fA-F]{6}$") : "Invalid " + field + " color format: " + color;
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

	private static <T> T getEither(Either<T, T> either) {
		return either.left().orElseGet(() -> either.right().orElseThrow());
	}

	/**
	 * @return The color object of the fog during the night during a full moon.
	 */
	public Color getNightFullMoonColor() {
		if (this.nightFullMoonCached == null) {
			assert night != null; // either night was set during serialization or nightFullMoonCached was set directly
			this.nightFullMoonCached = Color.parse(
					getEither(night.mapBoth(
							oldNight -> oldNight,
							NightColors::nightFullMoon
					)));
		}
		return this.nightFullMoonCached;
	}

	/**
	 * @return The color object of the fog during the night during a new moon.
	 */
	public Color getNightNewMoonColor() {
		if (this.nightNewMoonCached == null && night != null) {
			night.ifRight(nightColors -> {
				this.nightNewMoonCached = Color.parse(nightColors.nightNewMoon());
			});
		}

		return this.nightNewMoonCached;
	}

	/**
	 * @return The color of the fog during the day - in #RRGGBB format.
	 */
	private @NotNull String getDay() {
		return day;
	}

	/**
	 * @return The color of the fog during the night - in #RRGGBB format or an object defining both nightFullMoon and nightNewMoon color in #RRGGBB format.
	 */
	private @Nullable Either<String, NightColors> getNight() {
		return night;
	}

	/**
	 * A builder for fog colors.
	 */
	public static class Builder {
		private @NotNull String day = DEFAULT_SURFACE_DAY_COLOR;
		private @NotNull String nightFullMoon = DEFAULT_SURFACE_NIGHT_COLOR;
		private @Nullable String nightNewMoon = null;

		/**
		 * @param day The color of the fog during the day - in #RRGGBB format.
		 * @return The builder.
		 */
		public @NotNull Builder day(@NotNull String day) {
			this.day = day;
			return this;
		}

		/**
		 * @param nightFullMoon The color of the fog during the night during full moon - in #RRGGBB format.
		 * @return The builder.
		 */
		public @NotNull Builder nightFullMoon(@NotNull String nightFullMoon) {
			this.nightFullMoon = nightFullMoon;
			return this;
		}

		/**
		 * @param nightNewMoon The color of the fog during the night during new moon - in #RRGGBB format.
		 * @return The builder.
		 */
		public @NotNull Builder nightNewMoon(@Nullable String nightNewMoon) {
			this.nightNewMoon = nightNewMoon;
			return this;
		}

		/**
		 * @return The fog colors built from the builder, or using the default values if not specified.
		 */
		public @NotNull FogColors build() {
			return new FogColors(day, nightFullMoon, nightNewMoon);
		}
	}
}
