package dev.imb11.fog.client.util.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PlayerUtil {
	public static boolean isPlayerAboveGround(final double playerEyePositionY, final int seaLevel, final int worldTopYAtPlayerPosition) {
		// There need to be a minimum of 5 blocks above the player to be considered to be
		// underground. This is to prevent the fog from changing when the player is in a forest or building etc.
		return playerEyePositionY > worldTopYAtPlayerPosition - 5;
	}
}
