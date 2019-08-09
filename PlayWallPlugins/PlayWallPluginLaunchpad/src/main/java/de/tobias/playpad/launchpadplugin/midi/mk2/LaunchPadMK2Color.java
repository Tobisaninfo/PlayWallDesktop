package de.tobias.playpad.launchpadplugin.midi.mk2;

import de.thecodelabs.midi.feedback.FeedbackColor;
import de.tobias.playpad.action.feedback.LightMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum LaunchPadMK2Color implements FeedbackColor, LightMode.ILightMode {

	// White
	C0_1(1, Color.rgb(255, 255, 255), LightMode.LOW),
	C0_2(2, Color.rgb(255, 255, 255), LightMode.NORMAL),
	C0_3(3, Color.rgb(255, 255, 255), LightMode.HIGH),

	// RED
	C1_1(4, Color.rgb(255, 0, 0), LightMode.HIGH),
	C1_2(5, Color.rgb(255, 0, 0), LightMode.NORMAL),
	C1_3(6, Color.rgb(255, 0, 0), LightMode.MIDDLE),
	C1_4(7, Color.rgb(255, 0, 0), LightMode.LOW),

	// Orange
	C2_1(8, Color.rgb(255, 127, 0), LightMode.HIGH),
	C2_2(9, Color.rgb(255, 127, 0), LightMode.NORMAL),
	C2_3(10, Color.rgb(255, 127, 0), LightMode.MIDDLE),
	C2_4(11, Color.rgb(255, 127, 0), LightMode.LOW),

	// LIME
	C3_1(12, Color.rgb(235, 255, 39), LightMode.HIGH),
	C3_2(13, Color.rgb(235, 255, 39), LightMode.NORMAL),
	C3_3(14, Color.rgb(235, 255, 39), LightMode.MIDDLE),
	C3_4(15, Color.rgb(235, 255, 39), LightMode.LOW),

	// LIGHT GREEN
	C4_1(16, Color.rgb(123, 255, 66), LightMode.HIGH),
	C4_2(17, Color.rgb(123, 255, 66), LightMode.NORMAL),
	C4_3(18, Color.rgb(123, 255, 66), LightMode.MIDDLE),
	C4_4(19, Color.rgb(123, 255, 66), LightMode.LOW),

	// GREEN
	C5_1(20, Color.rgb(0, 255, 0), LightMode.HIGH),
	C5_2(21, Color.rgb(0, 255, 0), LightMode.NORMAL),
	C5_3(22, Color.rgb(0, 255, 0), LightMode.MIDDLE),
	C5_4(23, Color.rgb(0, 255, 0), LightMode.LOW),

	// GREEN
	C6_1(24, Color.rgb(62, 255, 112), LightMode.HIGH),
	C6_2(25, Color.rgb(62, 255, 112), LightMode.NORMAL),
	C6_3(26, Color.rgb(62, 255, 112), LightMode.MIDDLE),
	C6_4(27, Color.rgb(62, 255, 112), LightMode.LOW),

	// TURKEY
	C7_1(28, Color.rgb(62, 255, 112), LightMode.HIGH),
	C7_2(29, Color.rgb(62, 255, 112), LightMode.NORMAL),
	C7_3(30, Color.rgb(62, 255, 112), LightMode.MIDDLE),
	C7_4(31, Color.rgb(62, 255, 112), LightMode.LOW),

	// TURKEY
	C8_1(32, Color.rgb(101, 255, 196), LightMode.HIGH),
	C8_2(33, Color.rgb(101, 255, 196), LightMode.NORMAL),
	C8_3(34, Color.rgb(101, 255, 196), LightMode.MIDDLE),
	C8_4(35, Color.rgb(101, 255, 196), LightMode.LOW),

	// LIGHT BLUE
	C9_1(36, Color.rgb(91, 255, 253), LightMode.HIGH),
	C9_2(37, Color.rgb(91, 255, 253), LightMode.NORMAL),
	C9_3(38, Color.rgb(91, 255, 253), LightMode.MIDDLE),
	C9_4(39, Color.rgb(91, 255, 253), LightMode.LOW),

	// BLUE
	C10_1(40, Color.rgb(69, 169, 255), LightMode.HIGH),
	C10_2(41, Color.rgb(69, 169, 255), LightMode.NORMAL),
	C10_3(42, Color.rgb(69, 169, 255), LightMode.MIDDLE),
	C10_4(43, Color.rgb(69, 169, 255), LightMode.LOW),

	// DARK BLUE
	C11_1(44, Color.rgb(30, 67, 255), LightMode.HIGH),
	C11_2(45, Color.rgb(30, 67, 255), LightMode.NORMAL),
	C11_3(46, Color.rgb(30, 67, 255), LightMode.MIDDLE),
	C11_4(47, Color.rgb(30, 67, 255), LightMode.LOW),

	// PURPLE
	C12_1(48, Color.rgb(125, 73, 255), LightMode.HIGH),
	C12_2(49, Color.rgb(125, 73, 255), LightMode.NORMAL),
	C12_3(50, Color.rgb(125, 73, 255), LightMode.MIDDLE),
	C12_4(51, Color.rgb(125, 73, 255), LightMode.LOW),

	// VIOLET
	C13_1(52, Color.rgb(254, 85, 255), LightMode.HIGH),
	C13_2(53, Color.rgb(254, 85, 255), LightMode.NORMAL),
	C13_3(54, Color.rgb(254, 85, 255), LightMode.MIDDLE),
	C13_4(55, Color.rgb(254, 85, 255), LightMode.LOW),

	// VIOLET
	C14_1(56, Color.rgb(255, 75, 191), LightMode.HIGH),
	C14_2(57, Color.rgb(255, 75, 191), LightMode.NORMAL),
	C14_3(58, Color.rgb(255, 75, 191), LightMode.MIDDLE),
	C14_4(59, Color.rgb(255, 75, 191), LightMode.LOW),

	// BROWN
	C15_1(60, Color.rgb(255, 100, 69), LightMode.HIGH),
	C15_2(61, Color.rgb(255, 100, 69), LightMode.NORMAL),
	C15_3(62, Color.rgb(255, 100, 69), LightMode.MIDDLE),
	C15_4(63, Color.rgb(255, 100, 69), LightMode.LOW);

	private int midi;
	private Color color;
	private LightMode lightMode;

	LaunchPadMK2Color(int midi, Color color, LightMode lightMode) {
		this.midi = midi;
		this.color = color;
		this.lightMode = lightMode;
	}


	@Override
	public Paint getColor() {
		return color;
	}

	@Override
	public byte getValue() {
		return (byte) midi;
	}

	@Override
	public LightMode getLightMode() {
		return lightMode;
	}

	@Override
	public FeedbackColor translate(LightMode lightMode) {
		for (LaunchPadMK2Color instance : values()) {
			if (instance.getColor().equals(this.getColor())) {
				if (instance.lightMode == lightMode) {
					return instance;
				}
			}
		}
		return null;
	}


	public static FeedbackColor valueOf(int id) {
		for (LaunchPadMK2Color color : values()) {
			if (color.getValue() == id) {
				return color;
			}
		}
		return null;
	}
}
