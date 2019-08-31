package de.tobias.playpad.design.modern;

import de.thecodelabs.storage.settings.Storage;
import de.thecodelabs.storage.settings.StorageTypes;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.design.modern.model.ModernColorBean;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.util.FadeableColor;
import javafx.scene.paint.*;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

public enum ModernColor implements DisplayableColor {

	RED1,
	RED2,
	RED3,

	DARK_RED1,
	DARK_RED2,
	DARK_RED3,

	PINK1,
	PINK2,
	PINK3,

	PURPLE1,
	PURPLE2,
	PURPLE3,

	LIGHT_BLUE1,
	LIGHT_BLUE2,
	LIGHT_BLUE3,

	BLUE1,
	BLUE2,
	BLUE3,

	LIGHT_GREEN1,
	LIGHT_GREEN2,
	LIGHT_GREEN3,

	LIME1,
	LIME2,
	LIME3,

	YELLOW1,
	YELLOW2,
	YELLOW3,

	ORANGE1,
	ORANGE2,
	ORANGE3,

	GRAY1,
	GRAY2,
	GRAY3,
	GRAY4,
	GRAY5,
	GRAY6;

	private static final ModernColorBean[] colors;

	static {
		final InputStream inputStream = ApplicationUtils.getApplication().getClasspathResource("colors", "ModernColor.json").getInputStream();
		colors = Storage.load(inputStream, StorageTypes.JSON, ModernColorBean[].class);
	}

	private Optional<ModernColorBean> getCurrentModernColor() {
		return Stream.of(colors).filter(color -> color.getName().equals(name())).findAny();
	}

	public String getColorHi() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getHi();
	}

	public String getColorLow() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getLow();
	}

	public String getFontColor() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getFont();
	}

	public String getButtonColor() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getButton();
	}

	public String getPlaybarColor() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getPlaybar().getBackground();
	}

	public String getPlaybarTrackColor() {
		return getCurrentModernColor().orElseThrow(IllegalArgumentException::new).getColors().getPlaybar().getTrack();
	}

	@Override
	public Paint getColor() {
		if (Profile.currentProfile().getProfileSettings().getDesign().isFlatDesign()) {
			return Color.web(paint());
		} else {
			return new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web(getColorHi())),
					new Stop(1, Color.web(getColorLow())));
		}
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
