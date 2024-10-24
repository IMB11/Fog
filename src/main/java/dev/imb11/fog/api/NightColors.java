package dev.imb11.fog.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public record NightColors(@NotNull String nightFullMoon, @NotNull String nightNewMoon) {
	public static final Codec<NightColors> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("nightFullMoon").forGetter(NightColors::nightFullMoon),
			Codec.STRING.fieldOf("nightNewMoon").forGetter(NightColors::nightNewMoon)
	).apply(instance, NightColors::new));
}
