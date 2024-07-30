package dev.imb11.fog.client.util.math;

import dev.imb11.fog.api.FogColors;
import dev.imb11.fog.client.FogManager;
import dev.imb11.fog.client.util.color.Color;
import dev.imb11.fog.config.FogConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class HazeCalculator {
	private static float timeAboveSurface = 0;
	public static int[] times = null;
	public static float[] hazeValues = null;

	public static void initialize() {
		ArrayList<Integer> timesList = new ArrayList<>();
		ArrayList<Float> hazeValuesList = new ArrayList<>();

		var timeHazeEntries = FogConfig.getInstance().timeToHazeMap.entrySet();

		// Sort timeHazeEntries by time
		timeHazeEntries = timeHazeEntries.stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toCollection(LinkedHashSet::new));

		for (Map.Entry<Integer, Float> integerFloatEntry : timeHazeEntries) {
			timesList.add(integerFloatEntry.getKey());
			hazeValuesList.add(integerFloatEntry.getValue());
		}
		times = new int[timesList.size()];
		hazeValues = new float[hazeValuesList.size()];
		for (int i = 0; i < timesList.size(); i++) {
			times[i] = timesList.get(i);
			hazeValues[i] = hazeValuesList.get(i);
		}
	}

    public static FogManager.FogSettings applyHaze(float undergroundFactor, FogManager.FogSettings settings, int timeOfDay) {
		if(FogConfig.getInstance().disableHazeCalculation) {
			return settings;
		}

	    MinecraftClient client = MinecraftClient.getInstance();
	    ClientWorld world = client.world;
		ClientPlayerEntity player = client.player;
		double hazeValue = getHaze(timeOfDay);

	    FogColors belowGroundColors = FogColors.DEFAULT_CAVE;
	    FogColors aboveGroundColors = FogColors.DEFAULT;
	    Color aboveColor = timeOfDay >= 13000 && timeOfDay <= 23000 ? aboveGroundColors.getNightColor() : aboveGroundColors.getDayColor();
		Color belowColor = timeOfDay >= 13000 && timeOfDay <= 23000 ? belowGroundColors.getNightColor() : belowGroundColors.getDayColor();

        float fogColorR = (float) MathHelper.lerp(hazeValue, aboveColor.red / 255f, settings.fogRed());
        float fogColorG = (float) MathHelper.lerp(hazeValue, aboveColor.green / 255f, settings.fogGreen());
        float fogColorB = (float) MathHelper.lerp(hazeValue, aboveColor.blue / 255f, settings.fogBlue());

	    fogColorR = MathHelper.lerp(undergroundFactor, belowColor.red / 255f, fogColorR);
	    fogColorG = MathHelper.lerp(undergroundFactor, belowColor.green / 255f, fogColorG);
	    fogColorB = MathHelper.lerp(undergroundFactor, belowColor.blue / 255f, fogColorB);

	    int surfaceTopLevel = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) player.getX(), (int) player.getZ());
	    int viewCutoff = surfaceTopLevel + (client.options.getViewDistance().getValue() * 32);
	    int distancePastCutoff = (int) (player.getY() - viewCutoff);

		// TODO: Try and do something with fogStart here, eg: when fogStart as a blockpos is above the top level, start lerping.
	    // Check if player cannot see the top of the surface using their viewDistance option
	    if (client.player.getY() > viewCutoff) {
		    // Range from viewCutoff to viewCutoff + 25
		    float percentageCutoff = MathHelper.clamp(distancePastCutoff / 25f, 0, 1);
		    Vec3d skyColour = client.world.getSkyColor(player.getPos(), client.getTickDelta());
		    // Lerp between the fog color and the sky color
		    fogColorR = (float) MathHelper.lerp(percentageCutoff, fogColorR, skyColour.x);
		    fogColorG = (float) MathHelper.lerp(percentageCutoff, fogColorG, skyColour.y);
		    fogColorB = (float) MathHelper.lerp(percentageCutoff, fogColorB, skyColour.z);
	    }

        return new FogManager.FogSettings(settings.fogStart(), settings.fogEnd(), fogColorR, fogColorG, fogColorB);
    }

    public static double getHaze(int time) {
		if(times == null || hazeValues == null) {
			initialize();
		}

        // Ensure time is within the valid range
        if (time < 0 || time > 24000) {
	        time = time % 24000;
			if (time < 0) {
				time = 0;
			}
        }

        // Handle exact matches or boundaries
        for (int i = 0; i < times.length; i++) {
            if (time == times[i]) {
                return hazeValues[i];
            }
        }

        // Find the interval in which the time falls
        for (int i = 0; i < times.length - 1; i++) {
            if (time > times[i] && time < times[i + 1]) {
                // Linear interpolation between haze values
                double t1 = times[i];
                double t2 = times[i + 1];
                double h1 = hazeValues[i];
                double h2 = hazeValues[i + 1];

                // Interpolate
                return h1 + (h2 - h1) * (time - t1) / (t2 - t1);
            }
        }

        // If time doesn't fit into any interval, it means it matches exactly the last point
        return hazeValues[hazeValues.length - 1];
    }

//    public static void main(String[] args) {
//        // Test the function with different times, draw to an image
//        BufferedImage image = new BufferedImage(2400, 150, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = image.createGraphics();
//
//        for (int i = 0; i < 2400; i++) {
//            double haze = getHaze(i * 10);
//            graphics.setColor(new Color(0, 0, 0, (int) (255 * haze)));
//            graphics.drawLine(i, 0, i, 150);
//        }
//
//        graphics.dispose();
//    }
}
