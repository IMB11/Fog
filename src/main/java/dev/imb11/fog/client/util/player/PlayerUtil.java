package dev.imb11.fog.client.util.player;

import dev.imb11.fog.client.FogClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.Heightmap;

public class PlayerUtil {
	private static boolean cachedResult = false;
	private static long lastCheckTime = -1;

	public static boolean isPlayerAboveGround(ClientPlayerEntity clientPlayerEntity) {
		// Cache the result for 0.25 seconds
		if (System.currentTimeMillis() - lastCheckTime < 250) {
			return cachedResult;
		}

		lastCheckTime = System.currentTimeMillis();
		cachedResult = isPlayerAboveGroundImpl(clientPlayerEntity);
		return cachedResult;
	}

	private static boolean isPlayerAboveGroundImpl(ClientPlayerEntity clientPlayerEntity) {
		if (clientPlayerEntity.isSubmergedInWater()) return true;

		// Check 10 points around the player to see if they are above ground, 65% of the points must be underground to return false
		double x = clientPlayerEntity.getX();
		double z = clientPlayerEntity.getZ();
		double y = clientPlayerEntity.getEyeY();
		ClientWorld world = clientPlayerEntity.clientWorld;

		// random point within 5x5 radius of player
		int points = 10;
		int undergroundPoints = 0;
		for (int i = 0; i < points; i++) {
			double dx = (Math.random() - 0.5) * 5;
			double dz = (Math.random() - 0.5) * 5;

			float topY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, (int) (x + dx), (int) (z + dz));
			float seaLevel = world.getSeaLevel() - 0.25f;

			// Offset topY using sea level info to prevent false positives, eg: house roofs etc.
			topY = Math.max(topY, seaLevel);

			if (y < topY && y < seaLevel) {
				undergroundPoints++;
			}
		}

		// Check point at player eyes too.
		int topY = world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, (int) x, (int) z);
		int seaLevel = world.getSeaLevel();
		if(y < topY && y < seaLevel - 0.25f) {
			undergroundPoints++;
		}

		// Return if player is above ground
		return undergroundPoints < points * 0.65;
	}
}
