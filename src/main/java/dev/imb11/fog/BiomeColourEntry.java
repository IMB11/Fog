package dev.imb11.fog;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record BiomeColourEntry(Identifier biomeID, float fogR, float fogG, float fogB) {
    public static final ArrayList<BiomeColourEntry> BIOME_COLOURS = new ArrayList<>();

    public static BiomeColourEntry getOrDefault(@Nullable Identifier biomeID) {
        BiomeColourEntry defaultEntry = new BiomeColourEntry(Identifier.of("minecraft", "null"), 0.68f, 0.83f, 1f);
        if (biomeID == null) {
            return defaultEntry;
        }

        for (BiomeColourEntry entry : BIOME_COLOURS) {
            if (entry.biomeID().equals(biomeID)) {
                return entry;
            }
        }

        return defaultEntry;
    }

    static {
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "the_nether"), 0.26f, 0f, 0f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "swamp"), 0.28f, 0.28f, 0.05f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "mangrove_swamp"), 0.28f, 0.28f, 0.05f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "jungle"), 0.15f, 0.42f, 0.13f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "bamboo_jungle"), 0.15f, 0.42f, 0.13f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "sparse_jungle"), 0.15f, 0.42f, 0.13f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "snowy_plains"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "snowy_slopes"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "snowy_taiga"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "snowy_beach"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "frozen_peaks"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "jagged_peaks"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "frozen_ocean"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "frozen_river"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "ice_spikes"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "grove"), 0.96f, 0.98f, 0.94f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "desert"), 0.84f, 0.78f, 0.6f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "badlands"), 0.75f, 0.4f, 0.13f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "eroded_badlands"), 0.75f, 0.4f, 0.13f));
        BIOME_COLOURS.add(new BiomeColourEntry(Identifier.of("minecraft", "wooded_badlands"), 0.75f, 0.4f, 0.13f));
    }
}
