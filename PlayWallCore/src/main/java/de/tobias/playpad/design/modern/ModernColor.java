package de.tobias.playpad.design.modern;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.util.FadeableColor;
import javafx.scene.paint.*;

public enum ModernColor implements DisplayableColor {

	// - Color Hi -- Color Low -- Font ---- Button -- Bar BG -- Bar Track
	RED1("#ef9a9a", "#ef5350", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	RED2("#ef5350", "#e53935", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	RED3("#e53935", "#c62828", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),

	// --------- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	DARK_RED1("#D92349", "#AD2039", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	DARK_RED2("#C92349", "#8D2039", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	DARK_RED3("#A90329", "#6D0019", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),

	// ---- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	PINK1("#f48fb1", "#ec407a", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	PINK2("#ec407a", "#d81b60", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	PINK3("#d81b60", "#ad1457", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),

	// -------- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	PURPLE1("#ce93d8", "#ab47bc", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	PURPLE2("#ab47bc", "#8e24aa", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),
	PURPLE3("#8e24aa", "#6a1b9a", "#FFFFFF", "#FFFFFF", "#FFFFFF", "#000000"),

	// ---- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	LIGHT_BLUE1("#80deea", "#26c6da", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIGHT_BLUE2("#26c6da", "#00acc1", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIGHT_BLUE3("#00acc1", "#00838f", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ---- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	BLUE1("#90caf9", "#42a5f5", "#000000", "#000000", "#FFFFFF", "#000000"),
	BLUE2("#42a5f5", "#1e88e5", "#000000", "#000000", "#FFFFFF", "#000000"),
	BLUE3("#1e88e5", "#1565c0", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ------------ BG ------ PLAY ----- Font ---- Button --- Bar BG -- Bar Track
	LIGHT_GREEN1("#c5e1a5", "#9ccc65", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIGHT_GREEN2("#9ccc65", "#7cb342", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIGHT_GREEN3("#7cb342", "#558b2f", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ---- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	LIME1("#e6ee9c", "#d4e157", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIME2("#d4e157", "#c0ca33", "#000000", "#000000", "#FFFFFF", "#000000"),
	LIME3("#c0ca33", "#9e9d24", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ------- BG ------ PLAY ------ Font ---- Button -- Bar BG -- Bar Track
	YELLOW1("#fff59d", "#ffee58", "#000000", "#000000", "#FFFFFF", "#000000"),
	YELLOW2("#ffee58", "#fdd835", "#000000", "#000000", "#FFFFFF", "#000000"),
	YELLOW3("#fdd835", "#f9a825", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ------- BG ------ PLAY ----- Font ---- Button -- Bar BG -- Bar Track
	ORANGE1("#ffcc80", "#ffa726", "#000000", "#000000", "#FFFFFF", "#000000"),
	ORANGE2("#ffa726", "#fb8c00", "#000000", "#000000", "#FFFFFF", "#000000"),
	ORANGE3("#fb8c00", "#ef6c00", "#000000", "#000000", "#FFFFFF", "#000000"),

	// ---- BG ------ PLAY ----- Font ---- Button --- Bar BG --- Bar Track
	GRAY1("#eeeeee", "#cccccc", "#000000", "#000000", "#000000", "#FFFFFF"),
	GRAY2("#cccccc", "#aaaaaa", "#000000", "#000000", "#000000", "#FFFFFF"),
	GRAY3("#aaaaaa", "#888888", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF"),
	GRAY4("#888888", "#666666", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF"),
	GRAY5("#666666", "#444444", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF"),
	GRAY6("#444444", "#222222", "#FFFFFF", "#FFFFFF", "#000000", "#FFFFFF");

	private final String colorHi;
	private final String colorLow;
	private final String fontColor;
	private final String buttonColor;
	private final String playbarColor;
	private final String playbarTrackColor;

	ModernColor(String colorHi, String colorLow, String fontColor, String buttonColor, String playbarColor, String playbarTrackColor) {
		this.colorHi = colorHi;
		this.colorLow = colorLow;
		this.fontColor = fontColor;
		this.buttonColor = buttonColor;
		this.playbarColor = playbarColor;
		this.playbarTrackColor = playbarTrackColor;
	}

	public String getColorHi() {
		return colorHi;
	}

	public String getColorLow() {
		return colorLow;
	}

	public String getFontColor() {
		return fontColor;
	}

	public String getButtonColor() {
		return buttonColor;
	}

	public String getPlaybarColor() {
		return playbarColor;
	}

	public String getPlaybarTrackColor() {
		return playbarTrackColor;
	}

	@Override
	public Paint getPaint() {
		return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web(colorHi)),
				new Stop(1, Color.web(colorLow)));
	}

	public String linearGradient() {
		return "linear-gradient(" + getColorHi() + "," + getColorLow() + ")";
	}

	public String paint() {
		return getColorLow();
	}

	public static ModernColor modernColorByBackgroundColor(String color) {
		for (ModernColor modernColor : ModernColor.values()) {
			if (modernColor.getColorHi().contains(color) || modernColor.getColorLow().contains(color)) {
				return modernColor;
			}
		}
		return null;
	}

	public FadeableColor toFlatFadeableColor() {
		return new FadeableColor(getColorLow());
	}

	public FadeableColor toFadeableColor() {
		return new FadeableColor(getColorHi(), getColorLow());
	}
}
