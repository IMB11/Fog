/*? if fabric {*/
package dev.imb11.fog.loaders.fabric.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.Nullable;

public class FogDatagenFabric implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(VanillaFogDefinitionProvider::new);
	}

	@Override
	public @Nullable String getEffectiveModId() {
		return "fog";
	}
}
/*?}*/
