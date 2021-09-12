package de.tobias.playpad.project;

import de.tobias.playpad.design.ModernDesignSizeHelper;
import javafx.stage.Screen;

public class ProjectSettingsValidator {
	public static final int DISPLAY_OFFSET = 100;

	public enum ValidationState {
		NORMAL,
		TOO_MUCH,
		TOO_LESS
	}

	public enum Dimension {
		COLUMNS,
		ROWS
	}

	public ProjectSettingsValidator(Screen mainWindowScreen) {
		this.mainWindowScreen = mainWindowScreen;
	}

	private final Screen mainWindowScreen;

	public ValidationState validate(int input, Dimension dimension) {
		if (input < minValue(dimension)) {
			return ValidationState.TOO_LESS;
		} else if (input > maxValue(dimension)) {
			return ValidationState.TOO_MUCH;
		}
		return ValidationState.NORMAL;
	}

	public int minValue(Dimension dimension) {
		if (dimension == Dimension.COLUMNS) {
			return ProjectSettings.MIN_COLUMNS;
		} else if (dimension == Dimension.ROWS) {
			return ProjectSettings.MIN_ROWS;
		}
		return -1;
	}

	public int maxValue(Dimension dimension) {
		double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
		double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

		if (dimension == Dimension.COLUMNS) {
			return Math.min((int) (width / ModernDesignSizeHelper.getPadWidth()), ProjectSettings.MAX_COLUMNS);
		} else if (dimension == Dimension.ROWS) {
			return Math.min((int) ((height - DISPLAY_OFFSET) / ModernDesignSizeHelper.getPadHeight()), ProjectSettings.MAX_ROWS);
		}
		return -1;
	}
}
