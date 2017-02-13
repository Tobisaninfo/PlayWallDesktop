package de.tobias.playpad.launchpadplugin.midi.device.mk2;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import javafx.scene.paint.Color;

public enum LaunchPadMK2Color implements DisplayableFeedbackColor {

	C0(0, Color.rgb(0, 0, 0)), // BLACK
	C2(2, Color.rgb(125, 125, 125)), // GRAY
	C3(3, Color.rgb(255, 255, 255)), // WHITE
	C4(4, Color.rgb(255, 74, 76)), // RED
	C5(5, Color.rgb(255, 0, 24)), // RED
	C6(6, Color.rgb(93, 1, 3)), // DARK_RED
	C8(8, Color.rgb(254, 188, 112)), // LIGHT_ORGANGE
	C9(9, Color.rgb(255, 83, 35)), // ORANGE
	C10(10, Color.rgb(92, 28, 6)), // BRAUN
	C11(11, Color.rgb(255, 189, 112)), // ORNAGE ! DARK
	C12(12, Color.rgb(253, 252, 91)), // YELLOW
	C13(13, Color.rgb(253, 252, 85)), // YELLOW
	C14(14, Color.rgb(88, 88, 24)), // YELLOW
	C16(16, Color.rgb(117, 253, 92)), // LIGHT_GREEN
	C17(17, Color.rgb(117, 253, 92)), // LIGHT_GREEN
	C21(21, Color.rgb(0, 253, 81)), // LIGHT_GREEN
	C25(25, Color.rgb(0, 253, 81)), // LIGHT_GREEN
	C28(28, Color.rgb(0, 253, 143)), // LIGHT GRREN
	C32(32, Color.rgb(0, 252, 184)), // TURQUOISE
	C36(36, Color.rgb(36, 194, 250)), // LIGHT_BLUE
	C37(37, Color.rgb(0, 169, 249)), // LIGHT_BLUE
	C40(40, Color.rgb(60, 137, 248)), // BLUE
	C41(41, Color.rgb(0, 87, 246)), // BLUE
	C45(45, Color.rgb(7, 32, 245)), // BLUE
	C52(52, Color.rgb(255, 78, 247)), // PURPLE
	C53(53, Color.rgb(255, 23, 246)), // PINK
	C56(56, Color.rgb(255, 76, 131)), // PINK
	C57(57, Color.rgb(255, 0, 83)), // PINK
	C58(58, Color.rgb(93, 1, 27)), // PINK
	C59(59, Color.rgb(34, 0, 16)), // PINK
	C60(60, Color.rgb(255, 12, 25)), // RED
	C61(61, Color.rgb(159, 52, 18)), // ORANGE !
	C67(67, Color.rgb(7, 32, 245)), // BLUE !
	C72(72, Color.rgb(255, 0, 24)), // RED
	C76(76, Color.rgb(0, 136, 40)), // GREEN
	C78(78, Color.rgb(0, 169, 249)), // LIGHT_BLUE
	C79(79, Color.rgb(5, 44, 245)), // BLUE
	C90(90, Color.rgb(0, 252, 207)), // TURKY
	C81(81, Color.rgb(123, 31, 245)), // PURPULE
	C82(82, Color.rgb(185, 24, 123)), // PURPULE
	C84(84, Color.rgb(255, 71, 32)), // ORANGE !
	C88(88, Color.rgb(0, 253, 81)), // GREEN !
	C92(92, Color.rgb(37, 81, 194)), // BLUE
	C96(96, Color.rgb(255, 189, 112)), // ORANGE
	C106(106, Color.rgb(176, 0, 12)), // RED
	C107(107, Color.rgb(230, 79, 61)), // RED
	C116(116, Color.rgb(142, 102, 247)), // PURPLE
	C119(119, Color.rgb(221, 252, 252)), // WHITE
	C120(120, Color.rgb(168, 2, 12)); // RED

	private int midi;
	private Color color;

	LaunchPadMK2Color(int midi, Color color) {
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
		for (LaunchPadMK2Color color : values()) {
			if (color.mapperFeedbackValue() == id) {
				return color;
			}
		}
		return null;
	}
}
