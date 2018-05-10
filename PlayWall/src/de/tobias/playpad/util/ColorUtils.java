package de.tobias.playpad.util;

import com.itextpdf.text.BaseColor;
import javafx.scene.paint.Color;

/**
 * Created by tobias on 26.03.17.
 */
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

	public static BaseColor toBaseColor(String hexCode) {
		hexCode = hexCode.replace("#", "");
		return new BaseColor(
				Integer.valueOf(hexCode.substring(0, 2), 16),
				Integer.valueOf(hexCode.substring(2, 4), 16),
				Integer.valueOf(hexCode.substring(4, 6), 16)
		);
	}
}
