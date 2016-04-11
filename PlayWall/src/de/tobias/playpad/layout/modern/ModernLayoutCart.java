package de.tobias.playpad.layout.modern;

import org.dom4j.Element;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.FadeableColor;
import de.tobias.playpad.layout.Layout;
import de.tobias.playpad.layout.LayoutColorAssociator;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.Warning;
import de.tobias.playpad.pad.conntent.Durationable;
import de.tobias.playpad.pad.view.IPadViewController;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ModernLayoutCart extends Layout implements CartLayout, LayoutColorAssociator {

	public static final String TYPE = "modern";

	public static final double minWidth = 205;
	public static final double minHeight = 110;

	private ModernColor backgroundColor = ModernColor.GRAY1;
	private ModernColor playColor = ModernColor.RED1;

	private boolean isWarnAnimation = true;

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

	public void reset() {
		backgroundColor = ModernColor.GRAY1;
		playColor = ModernColor.RED1;

		isWarnAnimation = true;
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
	}

	@Override
	public void save(Element rootElement) {
		rootElement.addElement("BackgroundColor").addText(backgroundColor.name());
		rootElement.addElement("PlayColor").addText(playColor.name());
		Element animationElement = rootElement.addElement("Animation");
		animationElement.addElement("Warn").addText(String.valueOf(isWarnAnimation));
	}

	// Warn Handler -> Animation oder Blinken
	@Override
	public void handleWarning(IPadViewController controller, Warning warning) {
		if (isWarnAnimation) {
			warnAnimation(controller, warning);
		} else {
			ModernLayoutAnimator.warnFlash(controller);
		}
	}

	@Override
	public void stopWarning(IPadViewController controller) {
		ModernLayoutAnimator.stopAnimation(controller);
	}

	private void warnAnimation(IPadViewController controller, Warning warning) {
		FadeableColor stopColor = new FadeableColor(this.backgroundColor.getColorHi(), this.backgroundColor.getColorLow());
		FadeableColor playColor = new FadeableColor(this.playColor.getColorHi(), this.playColor.getColorLow());

		Duration warnDuration = warning.getTime();
		Pad pad = controller.getPad();

		if (pad.getContent() instanceof Durationable) {
			if (warnDuration.greaterThan(((Durationable) pad.getContent()).getDuration())) {
				warnDuration = ((Durationable) pad.getContent()).getDuration();
			}
		}

		ModernLayoutAnimator.animateWarn(controller, playColor, stopColor, warnDuration);
	}

	// Cart Layout
	@Override
	public String convertToCss(String prefix, boolean full) {
		StringBuilder builder = new StringBuilder();

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
		addStyleParameter(builder, "-fx-background-color", backgroundColor.linearGradient());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-info");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-title");
		addStyleParameter(builder, "-fx-text-fill", backgroundColor.getFontColor());
		endStyleClass(builder);

		buildCss(builder, PseudoClasses.PLAY_CALSS.getPseudoClassName(), prefix, playColor);
		buildCss(builder, PseudoClasses.WARN_CLASS.getPseudoClassName(), prefix, backgroundColor);

		return builder.toString().replace("0x", "#");
	}

	private void buildCss(StringBuilder builder, String state, String prefix, ModernColor color) {
		startStyleClass(builder, "pad" + prefix + "-info:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + "-title:" + state);
		addStyleParameter(builder, "-fx-text-fill", color.getFontColor());
		endStyleClass(builder);

		startStyleClass(builder, "pad" + prefix + ":" + state);
		addStyleParameter(builder, "-fx-background-color", color.linearGradient());
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

	// Color Associator
	@Override
	public Color getAssociatedEventColor() {
		return Color.web(playColor.getColorHi());
	}

	@Override
	public Color getAssociatedStandardColor() {
		return Color.web(backgroundColor.getColorHi());
	}
}
