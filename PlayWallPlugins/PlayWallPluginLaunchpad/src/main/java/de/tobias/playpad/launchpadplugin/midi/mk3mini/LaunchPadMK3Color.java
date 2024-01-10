package de.tobias.playpad.launchpadplugin.midi.mk3mini;

import de.thecodelabs.midi.feedback.FeedbackColor;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum LaunchPadMK3Color implements FeedbackColor {

	/*
	High
	Normal
	Low
	 */

	// White
	C0_3(3, Color.rgb(255, 255, 255)),
	C0_2(2, Color.rgb(255, 255, 255).darker()),
	C0_1(1, Color.rgb(255, 255, 255).darker().darker()),

	// RED
	C1_1(5, Color.rgb(255, 0, 0)),
	C1_2(6, Color.rgb(255, 0, 0).darker()),
	C1_3(7, Color.rgb(255, 0, 0).darker().darker()),

	// Orange
	C2_1(9, Color.rgb(255, 127, 0)),
	C2_2(10, Color.rgb(255, 127, 0).darker()),
	C2_3(11, Color.rgb(255, 127, 0).darker().darker()),

	// LIME
	C3_1(13, Color.rgb(235, 255, 39)),
	C3_2(14, Color.rgb(235, 255, 39).darker()),
	C3_3(15, Color.rgb(235, 255, 39).darker().darker()),

	// LIGHT GREEN
	C4_1(17, Color.rgb(123, 255, 66)),
	C4_2(18, Color.rgb(123, 255, 66).darker()),
	C4_3(19, Color.rgb(123, 255, 66).darker().darker()),

	// GREEN
	C5_1(21, Color.rgb(0, 255, 0)),
	C5_2(22, Color.rgb(0, 255, 0).darker()),
	C5_3(23, Color.rgb(0, 255, 0).darker().darker()),

	// GREEN
	C6_1(25, Color.rgb(62, 255, 112)),
	C6_2(26, Color.rgb(62, 255, 112).darker()),
	C6_3(27, Color.rgb(62, 255, 112).darker().darker()),

	// TURKEY
	C7_1(29, Color.rgb(62, 255, 112)),
	C7_2(30, Color.rgb(62, 255, 112).darker()),
	C7_3(31, Color.rgb(62, 255, 112).darker().darker()),

	// TURKEY
	C8_1(33, Color.rgb(101, 255, 196)),
	C8_2(34, Color.rgb(101, 255, 196).darker()),
	C8_3(35, Color.rgb(101, 255, 196).darker().darker()),

	// LIGHT BLUE
	C9_1(37, Color.rgb(91, 255, 253)),
	C9_2(38, Color.rgb(91, 255, 253).darker()),
	C9_3(39, Color.rgb(91, 255, 253).darker().darker()),

	// BLUE
	C10_1(41, Color.rgb(69, 169, 255)),
	C10_2(42, Color.rgb(69, 169, 255).darker()),
	C10_3(43, Color.rgb(69, 169, 255).darker().darker()),

	// DARK BLUE
	C11_1(45, Color.rgb(30, 67, 255)),
	C11_2(46, Color.rgb(30, 67, 255).darker()),
	C11_3(47, Color.rgb(30, 67, 255).darker().darker()),

	// PURPLE
	C12_1(49, Color.rgb(125, 73, 255)),
	C12_2(50, Color.rgb(125, 73, 255).darker()),
	C12_3(51, Color.rgb(125, 73, 255).darker().darker()),

	// VIOLET
	C13_1(53, Color.rgb(254, 85, 255)),
	C13_2(54, Color.rgb(254, 85, 255).darker()),
	C13_3(55, Color.rgb(254, 85, 255).darker().darker()),

	// VIOLET
	C14_1(57, Color.rgb(255, 75, 191)),
	C14_2(58, Color.rgb(255, 75, 191).darker()),
	C14_3(59, Color.rgb(255, 75, 191).darker().darker()),

	// BROWN
	C15_1(61, Color.rgb(255, 100, 69)),
	C15_2(62, Color.rgb(255, 100, 69).darker()),
	C15_3(63, Color.rgb(255, 100, 69).darker().darker());

	private final int midi;
	private final Color color;

	LaunchPadMK3Color(int midi, Color color) {
		this.midi = midi;
		this.color = color;
	}

	@Override
	public Paint getColor() {
		return color;
	}

	@Override
	public byte getValue() {
		return (byte) midi;
	}

	public static FeedbackColor valueOf(int id) {
		for (LaunchPadMK3Color color : values()) {
			if (color.getValue() == id) {
				return color;
			}
		}
		return null;
	}
}
