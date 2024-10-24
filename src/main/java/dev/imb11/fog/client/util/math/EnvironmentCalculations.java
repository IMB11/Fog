package dev.imb11.fog.client.util.math;

import dev.imb11.fog.api.FogColors;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.NotNull;

public class EnvironmentCalculations {
    public static FogManager.FogSettings apply(float undergroundFactor, FogManager.FogSettings settings, float tickDelta) {
	    return fixElytraColor(
				applyCaveFog(undergroundFactor, settings),
			    tickDelta,
			    MinecraftClient.getInstance(),
			    MinecraftClient.getInstance().player,
			    MinecraftClient.getInstance().world
	    );
    }

	private static FogManager.@NotNull FogSettings applyCaveFog(float undergroundFactor, FogManager.FogSettings settings) {
		FogColors belowGroundColors = FogColors.DEFAULT_CAVE;
		Color belowColor = belowGroundColors.getNightFullMoonColor();

		float fogColorR = MathHelper.lerp(undergroundFactor, belowColor.red / 255f, settings.fogRed()),
				fogColorG = MathHelper.lerp(undergroundFactor, belowColor.green / 255f, settings.fogGreen()),
				fogColorB = MathHelper.lerp(undergroundFactor, belowColor.blue / 255f, settings.fogBlue());

		return new FogManager.FogSettings(settings.fogStart(), settings.fogEnd(), fogColorR, fogColorG, fogColorB);
	}

	public static FogManager.FogSettings fixElytraColor(FogManager.FogSettings input, float tickDelta, MinecraftClient client, ClientPlayerEntity player, ClientWorld world) {
		float fogColorR = input.fogRed();
		float fogColorG = input.fogGreen();
		float fogColorB = input.fogBlue();

		int surfaceTopLevel = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) player.getX(), (int) player.getZ());
		int viewCutoff = surfaceTopLevel + (client.options.getViewDistance().getValue() * 32);
		int distancePastCutoff = (int) (player.getY() - viewCutoff);

		// TODO: Try and do something with fogStart here, eg: when fogStart as a blockpos is above the top level, start lerping.
		// Check if player cannot see the top of the surface using their viewDistance option
		if (client.player.getY() > viewCutoff) {
			// Range from viewCutoff to viewCutoff + 25
			float percentageCutoff = MathHelper.clamp(distancePastCutoff / 25f, 0, 1);
			Vec3d skyColour = client.world.getSkyColor(player.getPos(), tickDelta);
			// Lerp between the fog color and the sky color
			fogColorR = (float) MathHelper.lerp(percentageCutoff, fogColorR, skyColour.x);
			fogColorG = (float) MathHelper.lerp(percentageCutoff, fogColorG, skyColour.y);
			fogColorB = (float) MathHelper.lerp(percentageCutoff, fogColorB, skyColour.z);
		}

		return new FogManager.FogSettings(input.fogStart(), input.fogEnd(), fogColorR, fogColorG, fogColorB);
	}
}
