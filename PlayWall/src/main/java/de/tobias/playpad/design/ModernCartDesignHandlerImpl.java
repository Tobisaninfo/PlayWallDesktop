package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.view.PseudoClasses;
import javafx.util.Duration;

import static de.tobias.playpad.design.Design.*;

public class ModernCartDesignHandlerImpl implements ModernCartDesignHandler {

	// Warn Handler -> Animation oder Blinken
	@Override
	public void handleWarning(ModernCartDesign design, IPadViewController controller, Duration warning, ModernGlobalDesign globalDesign) {
		if (globalDesign.isWarnAnimation()) {
			warnAnimation(design, controller, warning);
		} else {
			ModernDesignAnimator.warnFlash(controller);
		}
	}

	@Override
	public void stopWarning(ModernCartDesign design, IPadViewController controller) {
		ModernDesignAnimator.stopAnimation(controller);
	}

	private void warnAnimation(ModernCartDesign design, IPadViewController controller, Duration warning) {
		ModernColor backgroundColor = design.getBackgroundColor();
		ModernColor playColor = design.getPlayColor();

		FadeableColor fadeStopColor = new FadeableColor(backgroundColor.getColorHi(), backgroundColor.getColorLow());
		FadeableColor fadePlayColor = new FadeableColor(playColor.getColorHi(), playColor.getColorLow());

		Pad pad = controller.getPad();

		if (pad.getContent() instanceof Durationable) {
			Duration padDuration = ((Durationable) pad.getContent()).getDuration();
			if (warning.greaterThan(padDuration)) {
				warning = padDuration;
			}
		}

		ModernDesignAnimator.animateWarn(controller, fadePlayColor, fadeStopColor, warning);
	}

	// Cart Layout
	@Override
	public String convertToCss(ModernCartDesign design, String prefix, boolean full, boolean flat) {
		StringBuilder builder = new StringBuilder();

		ModernColor backgroundColor = design.getBackgroundColor();

		startStyleClass(builder, "pad" + prefix + "-icon");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getButtonColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-playbar .track");
		addStyleParameter(builder, "-fx-base", backgroundColor.getPlaybarColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-playbar .bar");
		addStyleParameter(builder, "-fx-background-color", backgroundColor.getPlaybarTrackColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix);
		if (flat) {
			addStyleParameter(builder, "-fx-background-color", backgroundColor.paint());
		} else {
			addStyleParameter(builder, "-fx-background-color", backgroundColor.linearGradient());
		}
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-info");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-title");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		endStyleClass(builder);

		buildCss(builder, PseudoClasses.PLAY_CALSS.getPseudoClassName(), prefix, design.getPlayColor(), flat);
		buildCss(builder, PseudoClasses.WARN_CLASS.getPseudoClassName(), prefix, backgroundColor, flat);

		return builder.toString().replace("0x", "#");
	}

	private void buildCss(StringBuilder builder, String state, String prefix, ModernColor color, boolean flat) {
		startStyleClass(builder, "pad" + prefix + "-info:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-title:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + ":" + state);
		if (flat) {
			addStyleParameter(builder, "-fx-background-color", color.paint());
		} else {
			addStyleParameter(builder, "-fx-background-color", color.linearGradient());
		}
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-playbar:" + state + " .track");
		addStyleParameter(builder, "-fx-base", color.getPlaybarColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-playbar:" + state + " .bar");
		addStyleParameter(builder, "-fx-background-color", color.getPlaybarTrackColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-icon:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getButtonColor());
		endStyleClass(builder);
	}
}
