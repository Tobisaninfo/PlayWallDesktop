package de.tobias.playpad.plugin.playout;

import com.itextpdf.text.BaseColor;

/**
 * Created by tobias on 26.03.17.
 */
public class ColorUtils {

	private ColorUtils() {
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
