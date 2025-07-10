/*? if fabric {*/
package dev.imb11.fog.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.imb11.fog.config.FogConfig;

public class FogClientModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> FogConfig.getInstance().getYetAnotherConfigLibInstance().generateScreen(parent);
	}
}
/*?}*/
