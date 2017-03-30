package de.tobias.playpad.design.modern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.dom4j.Element;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.ColorModeHandler;
import de.tobias.playpad.design.Design;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.design.FadeableColor;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.ColorPickerView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ModernGlobalDesign extends Design implements GlobalDesign, DesignColorAssociator, ColorModeHandler {

	public static final double minWidth = 205;
	public static final double minHeight = 115;

	private ModernColor backgroundColor = ModernColor.GRAY1;
	private ModernColor playColor = ModernColor.RED3;

	private boolean isWarnAnimation = true;

	private int infoFontSize = 14;
	private int titleFontSize = 16;

	public ModernColor getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(ModernColor backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public ModernColor getPlayColor() {
		return playColor;
	}

	public void setPlayColor(ModernColor playColor) {
		this.playColor = playColor;
	}

	public boolean isWarnAnimation() {
		return isWarnAnimation;
	}

	public void setWarnAnimation(boolean isWarnAnimation) {
		this.isWarnAnimation = isWarnAnimation;
	}

	public int getInfoFontSize() {
		return infoFontSize;
	}

	public void setInfoFontSize(int infoFontSize) {
		this.infoFontSize = infoFontSize;
	}

	public int getTitleFontSize() {
		return titleFontSize;
	}

	public void setTitleFontSize(int titleFontSize) {
		this.titleFontSize = titleFontSize;
	}

	@Override
	public double getMinHeight(int rows) {
		return rows * minHeight;
	}

	@Override
	public double getMinWidth(int columns) {
		return columns * minWidth;
	}

	@Override
	public double getPadHeight() {
		return minHeight;
	}

	@Override
	public double getPadWidth() {
		return minWidth;
	}

	@Override
	public void reset() {
		backgroundColor = ModernColor.GRAY1;
		playColor = ModernColor.RED1;

		isWarnAnimation = true;

		infoFontSize = 14;
		titleFontSize = 16;
	}

	private String convertToCSS() {
		StringBuilder builder = new StringBuilder();

		startStyleClass(builder, "pad-icon");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getButtonColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar .track");
		addStyleParameter(builder, "-fx-base", backgroundColor.getPlaybarColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar .bar");
		addStyleParameter(builder, "-fx-background-color", backgroundColor.getPlaybarTrackColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad");
		addStyleParameter(builder, "-fx-background-color", backgroundColor.linearGradient());
		endStyleClass(builder);

		startStyleClass(builder, "pad-info");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		addStyleParameter(builder, "-fx-font-size", infoFontSize);
		endStyleClass(builder);

		startStyleClass(builder, "pad-title");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		addStyleParameter(builder, "-fx-font-size", titleFontSize);
		endStyleClass(builder);

		buildStateCss(builder, PseudoClasses.PLAY_CALSS.getPseudoClassName(), playColor);
		buildStateCss(builder, PseudoClasses.WARN_CLASS.getPseudoClassName(), backgroundColor);

		return builder.toString().replace("0x", "#");
	}

	private void buildStateCss(StringBuilder builder, String state, ModernColor color) {
		startStyleClass(builder, "pad-info:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-title:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad:" + state);
		addStyleParameter(builder, "-fx-background-color", color.linearGradient());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar:" + state + " .track");
		addStyleParameter(builder, "-fx-base", color.getPlaybarColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar:" + state + " .bar");
		addStyleParameter(builder, "-fx-background-color", color.getPlaybarTrackColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-icon:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getButtonColor());
		endStyleClass(builder);
	}

	@Override
	public void applyCss(Stage stage) {
		// Add Build in Default
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/style.css");
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/modern_style.css");

		// User Settings
		Path userCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "style.css");
		if (Files.exists(userCss))
			stage.getScene().getStylesheets().add(userCss.toUri().toString());
	}

	@Override
	public void applyCssMainView(IMainViewController controller, Stage stage, Project project) {
		applyCss(stage);

		controller.setGridColor(Color.TRANSPARENT);

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "custom_style.css");

		String css = convertToCSS();

		// Pad Spezelles Layout immer
		for (Pad pad : project.getPads()) {
			PadSettings padSettings = pad.getPadSettings();

			if (padSettings.isCustomDesign()) {
				CartDesign layoutOpt = padSettings.getOrCreateDesign(Profile.currentProfile().getProfileSettings().getLayoutType());
				css += "\n" + layoutOpt.convertToCss(pad.getPadIndex().toString(), true);
			}
		}

		// Speichern der generierten CSS Datei
		try {
			Files.write(path, css.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		stage.getScene().getStylesheets().remove(path.toUri().toString());
		stage.getScene().getStylesheets().add(path.toUri().toString());
	}

	@Override
	public void load(Element rootElement) {
		Element backgroundElement = rootElement.element("BackgroundColor");
		if (backgroundElement != null) {
			try {
				backgroundColor = ModernColor.valueOf(backgroundElement.getStringValue());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		Element playElement = rootElement.element("PlayColor");
		if (playElement != null) {
			try {
				playColor = ModernColor.valueOf(playElement.getStringValue());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		Element animationElement = rootElement.element("Animation");
		if (animationElement != null) {
			Element warnAnimationElement = animationElement.element("Warn");
			if (warnAnimationElement != null) {
				isWarnAnimation = Boolean.valueOf(warnAnimationElement.getStringValue());
			}
		}

		Element infoFontSizeElement = rootElement.element("InfoFontSize");
		if (infoFontSizeElement != null) {
			try {
				infoFontSize = Integer.valueOf(infoFontSizeElement.getStringValue());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		Element titleFontSizeElement = rootElement.element("TitleFontSize");
		if (titleFontSizeElement != null) {
			try {
				titleFontSize = Integer.valueOf(titleFontSizeElement.getStringValue());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void save(Element rootElement) {
		rootElement.addElement("BackgroundColor").addText(backgroundColor.name());
		rootElement.addElement("PlayColor").addText(playColor.name());
		Element animationElement = rootElement.addElement("Animation");
		animationElement.addElement("Warn").addText(String.valueOf(isWarnAnimation));
		rootElement.addElement("InfoFontSize").addText(String.valueOf(infoFontSize));
		rootElement.addElement("TitleFontSize").addText(String.valueOf(titleFontSize));
	}

	// Warn Handler -> Animation oder Blinken
	@Override
	public void handleWarning(IPadViewController controller, Duration warning) {
		if (isWarnAnimation) {
			warnAnimation(controller, warning);
		} else {
			ModernDesignAnimator.warnFlash(controller);
		}
	}

	@Override
	public void stopWarning(IPadViewController controller) {
		ModernDesignAnimator.stopAnimation(controller);
	}

	private void warnAnimation(IPadViewController controller, Duration warning) {
		FadeableColor stopColor = new FadeableColor(this.backgroundColor.getColorHi(), this.backgroundColor.getColorLow());
		FadeableColor playColor = new FadeableColor(this.playColor.getColorHi(), this.playColor.getColorLow());

		Pad pad = controller.getPad();

		if (pad.getContent() instanceof Durationable) {
			if (warning.greaterThan(((Durationable) pad.getContent()).getDuration())) {
				warning = ((Durationable) pad.getContent()).getDuration();
			}
		}

		ModernDesignAnimator.animateWarn(controller, playColor, stopColor, warning);
	}

	// Color Associator
	@Override
	public Color getAssociatedEventColor() {
		return Color.web(playColor.getColorHi());
	}

	@Override
	public Color getAssociatedStandardColor() {
		return Color.web(backgroundColor.getColorHi());
	}

	// Color View
	@Override
	public Node getColorInterface(Consumer<DisplayableColor> onSelection) {
		return new ColorPickerView(null, ModernColor.values(), onSelection);
	}

	@Override
	public void setColor(CartDesign design, DisplayableColor color) {
		if (design instanceof ModernCartDesign && color instanceof ModernColor) {
			((ModernCartDesign) design).setBackgroundColor((ModernColor) color);
		}
	}
}
