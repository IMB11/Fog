package dev.imb11.fog.client.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.fog.client.util.color.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class CustomFogDefinition {
	public static final Codec<CustomFogDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("start_multiplier").forGetter(CustomFogDefinition::getStartMultiplier),
			Codec.FLOAT.fieldOf("end_multiplier").forGetter(CustomFogDefinition::getEndMultiplier),
			FogColors.CODEC.optionalFieldOf("colors", null).forGetter(CustomFogDefinition::getColors)
	).apply(instance, CustomFogDefinition::new));

	private final float startMultiplier;
	private final float endMultiplier;
	private final @Nullable FogColors colors;

	public float getStartMultiplier() {
		return startMultiplier;
	}

	public float getEndMultiplier() {
		return endMultiplier;
	}

	public @Nullable FogColors getColors() {
		return colors;
	}

	public CustomFogDefinition(float startMultiplier, float endMultiplier, @Nullable FogColors colors) {
		this.startMultiplier = startMultiplier;
		this.endMultiplier = endMultiplier;
		this.colors = colors;
	}

	@Environment(EnvType.CLIENT)
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
