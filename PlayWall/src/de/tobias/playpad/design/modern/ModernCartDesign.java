package de.tobias.playpad.design.modern;

import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.DesignUpdateListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

import java.util.UUID;

public class ModernCartDesign extends Design implements CartDesign, DesignColorAssociator, Cloneable {

	public static final String TYPE = "modern";

	public static final double minWidth = 205;
	public static final double minHeight = 110;

	private UUID uuid;
	private ObjectProperty<ModernColor> backgroundColor;
	private ObjectProperty<ModernColor> playColor;

	private Pad pad;
	private DesignUpdateListener syncListener;

	ModernCartDesign(Pad pad) {
		this(pad, UUID.randomUUID());
	}

	public ModernCartDesign(Pad pad, UUID uuid) {
		this(pad, uuid, ModernColor.GRAY1, ModernColor.RED3);
	}

	public ModernCartDesign(Pad pad, UUID id, ModernColor backgroundColor, ModernColor playColor) {
		this.uuid = id;
		this.pad = pad;

		this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
		this.playColor = new SimpleObjectProperty<>(playColor);

		syncListener = new DesignUpdateListener(this);
	}



	public UUID getId() {
		return uuid;
	}

	public Pad getPad() {
		return pad;
	}

	public ModernColor getBackgroundColor() {
		return backgroundColor.get();
	}

	public void setBackgroundColor(ModernColor backgroundColor) {
		this.backgroundColor.set(backgroundColor);
	}

	public ObjectProperty<ModernColor> backgroundColorProperty() {
		return backgroundColor;
	}

	public ModernColor getPlayColor() {
		return playColor.get();
	}

	public void setPlayColor(ModernColor playColor) {
		this.playColor.set(playColor);
	}

	public ObjectProperty<ModernColor> playColorProperty() {
		return playColor;
	}

	@Override
	public void addListener() {
		syncListener.addListener();
	}

	public void removeListener() {
		syncListener.removeListener();
	}

	@Override
	public void reset() {
		backgroundColor.set(ModernColor.GRAY1);
		playColor.set(ModernColor.RED1);
	}

	// TODO Extract this loader and saver
	@Override
	public void load(Element rootElement) {
		uuid = UUID.fromString(rootElement.attributeValue("id"));

		Element backgroundElement = rootElement.element("BackgroundColor");
		if (backgroundElement != null) {
			try {
				backgroundColor.set(ModernColor.valueOf(backgroundElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		Element playElement = rootElement.element("PlayColor");
		if (playElement != null) {
			try {
				playColor.set(ModernColor.valueOf(playElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void save(Element rootElement) {
		rootElement.addAttribute("id", uuid.toString());
		rootElement.addElement("BackgroundColor").addText(backgroundColor.get().name());
		rootElement.addElement("PlayColor").addText(playColor.get().name());
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
		ModernColor backgroundColor = this.backgroundColor.get();
		ModernColor playColor = this.playColor.get();

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
	public String convertToCss(String prefix, boolean full) {
		StringBuilder builder = new StringBuilder();

		ModernColor backgroundColor = this.backgroundColor.get();

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

		buildCss(builder, PseudoClasses.PLAY_CALSS.getPseudoClassName(), prefix, playColor.get());
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
			backgroundColor.set(modernLayoutGlobal.getBackgroundColor());
			playColor.set(modernLayoutGlobal.getPlayColor());
		}
	}

	// Color Associator
	@Override
	public Color getAssociatedEventColor() {
		return Color.web(playColor.get().getColorHi());
	}

	@Override
	public Color getAssociatedStandardColor() {
		return Color.web(backgroundColor.get().getColorHi());
	}

	public ModernCartDesign clone(Pad pad) throws CloneNotSupportedException {
		ModernCartDesign clone = (ModernCartDesign) super.clone();
		clone.backgroundColor = new SimpleObjectProperty<>(getBackgroundColor());
		clone.playColor = new SimpleObjectProperty<>(getPlayColor());
		clone.pad = pad;
		clone.uuid = UUID.randomUUID();

		syncListener = new DesignUpdateListener(clone);
		if (pad.getProject().getProjectReference().isSync()) {
			addListener();
			CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), clone);
		}

		return clone;
	}

}
