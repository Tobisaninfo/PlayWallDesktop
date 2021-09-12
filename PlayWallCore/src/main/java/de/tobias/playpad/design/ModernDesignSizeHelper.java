package de.tobias.playpad.design;

public class ModernDesignSizeHelper {

	private static final double MIN_WIDTH = 140;
	private static final double MIN_HEIGHT = 115;

	private ModernDesignSizeHelper() {
	}

	public static double getMinHeight(int rows) {
		return rows * MIN_HEIGHT;
	}

	public static double getMinWidth(int columns) {
		return columns * MIN_WIDTH;
	}

	public static double getPadHeight() {
		return MIN_HEIGHT;
	}

	public static double getPadWidth() {
		return MIN_WIDTH;
	}
}
