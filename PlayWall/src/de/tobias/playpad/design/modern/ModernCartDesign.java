package de.tobias.playpad.design.modern;

import org.dom4j.Element;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.Design;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.design.FadeableColor;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ModernCartDesign extends Design implements CartDesign, DesignColorAssociator, Cloneable {

	public static final String TYPE = "modern";

	public static final double minWidth = 205;
	public static final double minHeight = 110;

	private ModernColor backgroundColor = ModernColor.GRAY1;
	private ModernColor playColor = ModernColor.RED3;

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

	@Override
	public void reset() {
		backgroundColor = ModernColor.GRAY1;
		playColor = ModernColor.RED1;
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
	}

	@Override
	public void save(Element rootElement) {
		rootElement.addElement("BackgroundColor").addText(backgroundColor.name());
		rootElement.addElement("PlayColor").addText(playColor.name());
	}

	// Warn Handler -> Animation oder Blinken
	@Override
	public void handleWarning(IPadViewController controller, Duration warning, GlobalDesign layout) {
		if (layout instanceof ModernGlobalDesign && ((ModernGlobalDesign) layout).isWarnAnimation()) {
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
			Duration padDuration = ((Durationable) pad.getContent()).getDuration();
			if (warning.greaterThan(padDuration)) {
				warning = padDuration;
			}
		}

		ModernDesignAnimator.animateWarn(controller, playColor, stopColor, warning);
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

	@Override
	public void copyGlobalLayout(GlobalDesign globalLayout) {
		if (globalLayout instanceof ModernGlobalDesign) {
			ModernGlobalDesign modernLayoutGlobal = (ModernGlobalDesign) globalLayout;
			backgroundColor = modernLayoutGlobal.getBackgroundColor();
			playColor = modernLayoutGlobal.getPlayColor();
		}
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

	@Override
	public Object clone() throws CloneNotSupportedException {
		ModernCartDesign clone = (ModernCartDesign) super.clone();
		clone.backgroundColor = backgroundColor;
		clone.playColor = playColor;
		return clone;
	}

}
