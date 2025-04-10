package dev.imb11.fog.mixin.compat;

import net.mehvahdjukaar.polytone.biome.BiomeEffectModifier;
import net.mehvahdjukaar.polytone.biome.BiomeEffectsManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Pseudo
@Mixin(BiomeEffectsManager.class)
public interface BiomeEffectsManagerAccessor {
	@Accessor("effectsToApply")
	Map<RegistryKey<Biome>, BiomeEffectModifier> getEffectsToApply();
}
