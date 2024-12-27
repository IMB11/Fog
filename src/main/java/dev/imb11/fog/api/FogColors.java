package dev.imb11.fog.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.imb11.fog.client.util.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Represents the colors of the fog during the day and night.
 */
public class FogColors {
	private static final HashMap<Class<? extends DimensionEffects>, FogColors> CACHED_DEFAULTS = new HashMap<>();

	public static FogColors getDefault(ClientWorld world) {
		if (world == null || world.getDimensionEffects() instanceof DimensionEffects.Overworld) {
			return new FogColors("#b9d2fd", "#000000");
		} else {
			if (CACHED_DEFAULTS.containsKey(world.getDimensionEffects().getClass())) {
				return CACHED_DEFAULTS.get(world.getDimensionEffects().getClass());
			} else {
				MinecraftClient client = MinecraftClient.getInstance();
				float sunHeight = MathHelper.clamp(MathHelper.cos(world.getSkyAngle(client.getRenderTickCounter().getTickDelta(true)) * 6.2831855F) * 2.0F + 0.5F, 0.0F, 1.0F);
				Vec3d daySky = world.getDimensionEffects().adjustFogColor(Vec3d.unpackRgb(((Biome)world.getBiomeAccess().getBiomeForNoiseGen(client.player.getX(), client.player.getY(), client.player.getZ()).value()).getFogColor()), sunHeight);
				Vec3d nightSky = daySky.multiply(0.2D);
				FogColors colors = new FogColors(Color.from(daySky).asHex(), Color.from(nightSky).asHex());
				CACHED_DEFAULTS.put(world.getDimensionEffects().getClass(), colors);
				return colors;
			}
		}
	}

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
		private @NotNull String day = getDefault(null).getDay();
		private @NotNull String night = getDefault(null).getNight();

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
