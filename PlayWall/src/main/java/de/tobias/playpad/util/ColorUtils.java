package de.tobias.playpad.util;

import javafx.scene.paint.Color;

public class ColorUtils {
	/**
	 * Get the color for the not found warning sign in a pad view.
	 *
	 * @param color background color
	 * @return sign color
	 */
	public static Color getWarningSignColor(Color color) {
		// Counting the perceptive luminance - human eye favors green color...
		double a = 1 - (0.299 * (int) (color.getRed() * 255) + 0.587 * (int) (color.getGreen() * 255) + 0.114 * (int) (color.getBlue() * 255)) / 255;

		if (a < 0.5) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}
}
