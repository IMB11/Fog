package dev.imb11.fog.client.util.player;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerUtil {
	private static boolean isPlayerAboveGroundCachedResult = false;
	private static long lastPlayerAboveGroundCheckTime = -1;

	public static boolean isPlayerAboveGround(ClientPlayerEntity clientPlayerEntity) {
		// Cache the result for 0.25 seconds
		if (System.currentTimeMillis() - lastPlayerAboveGroundCheckTime < 250) {
			return isPlayerAboveGroundCachedResult;
		}

		lastPlayerAboveGroundCheckTime = System.currentTimeMillis();
		isPlayerAboveGroundCachedResult = isPlayerAboveGroundImpl(clientPlayerEntity);
		return isPlayerAboveGroundCachedResult;
	}

	/**
	 * Checks if 10 random points within a 5x5 radius around the player are above ground, to determine if the player is above ground.
	 *
	 * @param clientPlayerEntity The player that should be checked.
	 * @return {@code true} if more than 65% of the points are above ground, {@code false}.
	 */
	private static boolean isPlayerAboveGroundImpl(@NotNull ClientPlayerEntity clientPlayerEntity) {
		if (clientPlayerEntity.isSubmergedInWater()) {
			return true;
		}

		@Nullable World clientWorld = clientPlayerEntity.
		//? if >1.21.8 {
		getEntityWorld();
		//?} else {
		/*clientWorld;
		*///?}
		if (clientWorld == null) {
			return true;
		}

		// Random point within 5x5 radius of player
		double x = clientPlayerEntity.getX();
		double z = clientPlayerEntity.getZ();
		double y = clientPlayerEntity.getEyeY();
		// Check 10 points around the player to see if they are above ground, 65% of the points must be underground to return false
		int points = 10;
		int undergroundPoints = 0;
		for (int i = 0; i < points; i++) {
			double dx = (Math.random() - 0.5) * 5;
			double dz = (Math.random() - 0.5) * 5;

			float topY = clientWorld.getTopY(Heightmap.Type.WORLD_SURFACE_WG, (int) (x + dx), (int) (z + dz));
			float seaLevel = clientWorld.getSeaLevel() - 0.25f;

			// Offset topY using sea level info to prevent false positives, eg: house roofs etc.
			topY = Math.max(topY, seaLevel);

			if (y < topY && y < seaLevel) {
				undergroundPoints++;
			}
		}

		// Check point at player eyes too.
		int topY = clientWorld.getTopY(Heightmap.Type.WORLD_SURFACE_WG, (int) x, (int) z);
		int seaLevel = clientWorld.getSeaLevel();
		if (y < topY && y < seaLevel - 0.25f) {
			undergroundPoints++;
		}

		// Return if player is above ground
		return undergroundPoints < points * 0.65;
	}
}
