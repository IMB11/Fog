package dev.imb11.fog.client.registry;

import dev.imb11.fog.client.resource.json.Fogs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class FogRegistry {
	public static final @NotNull Map<Identifier, Fogs> STRUCTURE_FOG_REGISTRY = new HashMap<>();
	public static final @NotNull Map<Identifier, Fogs> BIOME_FOG_REGISTRY = new HashMap<>();
}
