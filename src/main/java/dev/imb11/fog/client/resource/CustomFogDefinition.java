package dev.imb11.fog.client.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.fog.client.util.color.Color;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public record CustomFogDefinition(float startMultiplier, float endMultiplier, @Nullable FogColors colors) {
	public static final Codec<CustomFogDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("start_multiplier").forGetter(CustomFogDefinition::startMultiplier),
			Codec.FLOAT.fieldOf("end_multiplier").forGetter(CustomFogDefinition::endMultiplier),
			FogColors.CODEC.optionalFieldOf("colors", null).forGetter(CustomFogDefinition::colors)
	).apply(instance, CustomFogDefinition::new));

	
	public static class FogColors {
		public static final Codec<FogColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("day").forGetter(FogColors::getDay),
				Codec.STRING.fieldOf("night").forGetter(FogColors::getNight)
		).apply(instance, FogColors::new));

		public FogColors(@NotNull String day, @NotNull String night) {
			this.day = day;
			this.night = night;
		}

		private final @NotNull String day;
		private final @NotNull String night;

		private transient @Nullable Color dayCached;
		private transient @Nullable Color nightCached;

		public Color getDayColor() {
			if (dayCached == null) {
				dayCached = Color.parse(day);
			}

			return dayCached;
		}

		public Color getNightColor() {
			if (nightCached == null) {
				nightCached = Color.parse(night);
			}

			return nightCached;
		}

		private @NotNull String getDay() {
			return day;
		}

		private @NotNull String getNight() {
			return night;
		}
	}
}
