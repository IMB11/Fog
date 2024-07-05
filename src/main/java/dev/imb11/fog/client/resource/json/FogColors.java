package dev.imb11.fog.client.resource.json;

import dev.imb11.fog.client.util.color.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class FogColors {
	@SuppressWarnings("unused")
	private @Nullable String day;
	@SuppressWarnings("unused")
	private @Nullable String night;
	// TODO: Add interpolationType field

	private transient @Nullable Color dayCached;
	private transient @Nullable Color nightCached;

	public @Nullable Color getDayColor() {
		if (day == null) {
			return null;
		}
		if (dayCached == null) {
			dayCached = Color.parse(day);
		}

		return dayCached;
	}

	public @Nullable Color getNightColor() {
		if (night == null) {
			return null;
		}
		if (nightCached == null) {
			nightCached = Color.parse(night);
		}

		return nightCached;
	}
}
