package dev.imb11.fog.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a custom fog definition.
 */
public final class CustomFogDefinition {
	public static final CustomFogDefinition DEFAULT = new CustomFogDefinition(1.0f, 1.0f, FogColors.getDefault(null));
	public static final Codec<CustomFogDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("start_multiplier").forGetter(CustomFogDefinition::startMultiplier),
			Codec.FLOAT.fieldOf("end_multiplier").forGetter(CustomFogDefinition::endMultiplier),
			FogColors.CODEC.optionalFieldOf("colors", null).forGetter(CustomFogDefinition::colors)
	).apply(instance, CustomFogDefinition::new));

	private final float startMultiplier;
	private final float endMultiplier;
	private final @Nullable FogColors colors;

	/**
	 * @param startMultiplier The multiplier to apply to the fog start distance.
	 * @param endMultiplier   The multiplier to apply to the fog end distance.
	 * @param colors          The colors to apply to the fog.
	 */
	private CustomFogDefinition(float startMultiplier, float endMultiplier, @Nullable FogColors colors) {
		this.startMultiplier = startMultiplier;
		this.endMultiplier = endMultiplier;
		this.colors = colors;
	}

	/**
	 * @return The multiplier to apply to the fog start distance.
	 */
	public float startMultiplier() {return startMultiplier;}

	/**
	 * @return The multiplier to apply to the fog end distance.
	 */
	public float endMultiplier() {return endMultiplier;}

	/**
	 * @return The colors to apply to the fog, null if no colors are defined.
	 */
	public @Nullable FogColors colors() {return colors;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (CustomFogDefinition) obj;
		return Float.floatToIntBits(this.startMultiplier) == Float.floatToIntBits(that.startMultiplier) &&
				Float.floatToIntBits(this.endMultiplier) == Float.floatToIntBits(that.endMultiplier) &&
				Objects.equals(this.colors, that.colors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startMultiplier, endMultiplier, colors);
	}

	@Override
	public String toString() {
		return "CustomFogDefinition[" +
				"startMultiplier=" + startMultiplier + ", " +
				"endMultiplier=" + endMultiplier + ", " +
				"colors=" + colors + ']';
	}

	/**
	 * A builder for {@link CustomFogDefinition}.
	 */
	public static class Builder {
		private float startMultiplier = DEFAULT.startMultiplier();
		private float endMultiplier = DEFAULT.endMultiplier();
		private FogColors colors = DEFAULT.colors();

		public static Builder create() {
			return new Builder();
		}

		/**
		 * @param startMultiplier The multiplier to apply to the fog start distance.
		 * @return This builder.
		 */
		public Builder startMultiplier(float startMultiplier) {
			this.startMultiplier = startMultiplier;
			return this;
		}

		/**
		 * @param endMultiplier The multiplier to apply to the fog end distance.
		 * @return This builder.
		 */
		public Builder endMultiplier(float endMultiplier) {
			this.endMultiplier = endMultiplier;
			return this;
		}

		/**
		 * @param colors The colors to apply to the fog.
		 * @return This builder.
		 */
		public Builder colors(FogColors colors) {
			this.colors = colors;
			return this;
		}

		/**
		 * @param day The color of the fog during the day - in #RRGGBB format.
		 * @param night The color of the fog during the night - in #RRGGBB format.
		 * @return This builder.
		 */
		public Builder colors(String day, String night) {
			this.colors = new FogColors(day, night);
			return this;
		}

		/**
		 * @return A new custom fog definition using the supplied values or the defaults if not specified.
		 */
		public CustomFogDefinition build() {
			return new CustomFogDefinition(startMultiplier, endMultiplier, colors);
		}
	}
}
