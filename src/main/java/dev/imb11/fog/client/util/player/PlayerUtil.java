package dev.imb11.fog.client.util.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PlayerUtil {
	public static boolean isPlayerAboveGround(final double playerEyePositionY, final float worldSeaLevel, final int worldTopYAtPlayerPosition) {
		return playerEyePositionY + 0.5d > worldTopYAtPlayerPosition || playerEyePositionY + 0.5d > worldSeaLevel;
	}
}
