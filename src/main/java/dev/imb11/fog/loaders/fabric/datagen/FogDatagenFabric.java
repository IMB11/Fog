/*? if fabric && >=1.20.6 {*/
package dev.imb11.fog.loaders.fabric.datagen;

import dev.imb11.fog.client.FogClient;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FogDatagenFabric implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(@NotNull FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(VanillaFogDefinitionProvider::new);
	}

	@Override
	public @Nullable String getEffectiveModId() {
		return FogClient.MOD_ID;
	}
}
/*?}*/
