package dev.imb11.fog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class HazeCalculator {
    // Define the key time points and corresponding haze values
    private static final int[] times = {
            0, 500, 1500, 11500, 12500, 13500, 22500, 23500
    };

    private static final double[] hazeValues = {
            0.85, 0.25, 0.25, 0.25, 0.85, 0.5, 0.5, 0.85
    };

    public static double getHaze(int time) {
        // Ensure time is within the valid range
        if (time < 0 || time > 24000) {
            throw new IllegalArgumentException("Time must be between 0 and 24000 ticks.");
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

    public static void main(String[] args) {
        // Test the function with different times, draw to an image
        BufferedImage image = new BufferedImage(2400, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();

        for (int i = 0; i < 2400; i++) {
            double haze = getHaze(i * 10);
            graphics.setColor(new Color(0, 0, 0, (int) (255 * haze)));
            graphics.drawLine(i, 0, i, 150);
        }

        graphics.dispose();
    }
}
