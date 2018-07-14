package de.tobias.playpad.design.modern;

public class ModernDesignSizeHelper {

	public static final double minWidth = 165;
	public static final double minHeight = 115;

	public static double getMinHeight(int rows) {
		return rows * minHeight;
	}

	public static double getMinWidth(int columns) {
		return columns * minWidth;
	}

	public static double getPadHeight() {
		return minHeight;
	}

	public static double getPadWidth() {
		return minWidth;
	}
}
