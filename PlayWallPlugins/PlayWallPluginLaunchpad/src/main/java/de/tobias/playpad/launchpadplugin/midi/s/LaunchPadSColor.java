package de.tobias.playpad.launchpadplugin.midi.s;

import de.thecodelabs.midi.feedback.FeedbackColor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum LaunchPadSColor implements FeedbackColor {

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

	public static FeedbackColor valueOf(byte id) {
		for (LaunchPadSColor color : values()) {
			if (color.getValue() == id) {
				return color;
			}
		}
		return null;
	}

	@Override
	public Paint getColor() {
		return color;
	}

	@Override
	public byte getValue() {
		return (byte) midi;
	}
}
