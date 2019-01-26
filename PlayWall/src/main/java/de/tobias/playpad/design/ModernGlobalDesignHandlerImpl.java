package de.tobias.playpad.design;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.*;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.view.ColorPickerView;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import static de.tobias.playpad.design.Design.*;

public class ModernGlobalDesignHandlerImpl implements ModernGlobalDesignHandler, ColorModeHandler {

	// TODO Rewrite with expression parser
	private String convertToCSS(ModernGlobalDesign design) {
		StringBuilder builder = new StringBuilder();

		startStyleClass(builder, "pad-icon");
		addStyleParameter(builder, "-fx-text-fill", design.getBackgroundColor().getButtonColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar .track");
		addStyleParameter(builder, "-fx-base", design.getBackgroundColor().getPlaybarColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-playbar .bar");
		addStyleParameter(builder, "-fx-background-color", design.getBackgroundColor().getPlaybarTrackColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad");
		if (design.isFlatDesign()) {
			addStyleParameter(builder, "-fx-background-color", design.getBackgroundColor().paint());
		} else {
			addStyleParameter(builder, "-fx-background-color", design.getBackgroundColor().linearGradient());
		}
		endStyleClass(builder);

		startStyleClass(builder, "pad-info");
		addStyleParameter(builder, "-fx-text-fill", design.getBackgroundColor().getFontColor());
		addStyleParameter(builder, "-fx-font-size", design.getInfoFontSize());
		endStyleClass(builder);

		startStyleClass(builder, "pad-title");
		addStyleParameter(builder, "-fx-text-fill", design.getBackgroundColor().getFontColor());
		addStyleParameter(builder, "-fx-font-size", design.getTitleFontSize());
		endStyleClass(builder);

		buildStateCss(builder, PseudoClasses.PLAY_CLASS.getPseudoClassName(), design.getPlayColor(), design.isFlatDesign());
		buildStateCss(builder, PseudoClasses.WARN_CLASS.getPseudoClassName(), design.getBackgroundColor(), design.isFlatDesign());

		return builder.toString().replace("0x", "#");
	}

	private void buildStateCss(StringBuilder builder, String state, ModernColor color, boolean flat) {
		startStyleClass(builder, "pad-info:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad-title:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad:" + state);
		if (flat) {
			addStyleParameter(builder, "-fx-background-color", color.paint());
		} else {
			addStyleParameter(builder, "-fx-background-color", color.linearGradient());
		}
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
	public void applyStyleSheet(ModernGlobalDesign design, Stage stage) {
		// Add Build in Default
		stage.getScene().getStylesheets().add("style/style.css");
		stage.getScene().getStylesheets().add("style/modern.css");

		// User Settings
		Path userCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "style.css");
		if (Files.exists(userCss))
			stage.getScene().getStylesheets().add(userCss.toUri().toString());
	}

	@Override
	public void applyStyleSheetToMainViewController(ModernGlobalDesign design, IMainViewController controller, Stage stage, Project project) {
		applyStyleSheet(design, stage);

		controller.setGridColor(Color.TRANSPARENT);

		// Generate dynamic style sheet
		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "custom_style.css");
		StringBuilder css = new StringBuilder(convertToCSS(design));
		ModernCartDesignHandler cartDesignHandler = PlayPadPlugin.getModernDesignHandler().getModernCartDesignHandler();
		for (Pad pad : project.getPads()) {
			PadSettings padSettings = pad.getPadSettings();

			if (padSettings.isCustomDesign()) {
				ModernCartDesign cartDesign = padSettings.getDesign();
				css.append("\n").append(cartDesignHandler.convertToCss(cartDesign, pad.getPadIndex().toString(), design.isFlatDesign()));
			}
		}
		try {
			Files.write(path, css.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		stage.getScene().getStylesheets().remove(path.toUri().toString());
		stage.getScene().getStylesheets().add(path.toUri().toString());
	}

	@Override
	public void handleWarning(ModernGlobalDesign design, IPadViewController controller, Duration warning) {
		if (design.isWarnAnimation()) {
			warnAnimation(design, controller, warning);
		} else {
			ModernDesignAnimator.warnFlash(controller);
		}
	}


	@Override
	public void stopWarning(ModernGlobalDesign design, IPadViewController controller) {
		ModernDesignAnimator.stopAnimation(controller);
	}

	private void warnAnimation(ModernGlobalDesign design, IPadViewController controller, Duration warning) {
		FadeableColor stopColor = design.getBackgroundColor().toFadeableColor();
		FadeableColor playColor = design.getPlayColor().toFadeableColor();

		Pad pad = controller.getPad();

		if (pad.getContent() instanceof Durationable) {
			if (warning.greaterThan(((Durationable) pad.getContent()).getDuration())) {
				warning = ((Durationable) pad.getContent()).getDuration();
			}
		}

		ModernDesignAnimator.animateWarn(controller, playColor, stopColor, warning);
	}


	// Color View
	@Override
	public Node getColorInterface(Consumer<DisplayableColor> onSelection) {
		return new ColorPickerView(null, ModernColor.values(), onSelection);
	}

	@Override
	public void setColor(ModernCartDesign design, DisplayableColor color) {
		if (color instanceof ModernColor) {
			design.setBackgroundColor((ModernColor) color);
		}
	}
}
