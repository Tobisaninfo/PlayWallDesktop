package de.tobias.playpad.launchpadplugin.midi.s;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import javafx.scene.paint.Color;

public enum LaunchPadSColor implements DisplayableFeedbackColor {

	YELLOW(62, Color.YELLOW),
	AMBER(63, Color.WHEAT),
	GREEN(60, Color.GREEN),
	RED(15, Color.RED),
	BLACK(0, Color.BLACK);

	private int midi;
	private Color color;

	LaunchPadSColor(int midi, Color color) {
		this.midi = midi;
		this.color = color;
	}

	@Override
	public int mapperFeedbackValue() {
		return midi;
	}

	@Override
	public Color getPaint() {
		return color;
	}

	public static DisplayableFeedbackColor valueOf(int id) {
		for (LaunchPadSColor color : values()) {
			if (color.mapperFeedbackValue() == id) {
				return color;
			}
		}
		return null;
	}
}
