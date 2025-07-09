package dev.imb11.fog.client.compat.polytone;

import dev.imb11.fog.config.FogConfig;
import dev.imb11.mru.LoaderUtils;
import net.irisshaders.iris.Iris;

public class IrisCompat {
	public static boolean shouldDisableMod() {
		if (LoaderUtils.isModInstalled("iris") && FogConfig.getInstance().disableModWhenIrisShaderPackIsEnabled) {
			return Iris.isPackInUseQuick();
		} else {
			return false;
		}
	}
}
