package de.tobias.playpad.design.modern.model;

import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.DesignUpdateListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.UUID;

public class ModernCartDesign implements FeedbackDesignColorSuggester {

	public static final ModernColor DEFAULT_COLOR_BACKGROUND = ModernColor.GRAY1;
	public static final ModernColor DEFAULT_COLOR_PLAY = ModernColor.RED3;
	public static final ModernColor DEFAULT_COLOR_CUE_IN = ModernColor.RED2;

	private UUID uuid;
	private ObjectProperty<ModernColor> backgroundColor;
	private ObjectProperty<ModernColor> playColor;
	private ObjectProperty<ModernColor> cueInColor;

	private Pad pad;
	private DesignUpdateListener syncListener;

	public ModernCartDesign(Pad pad) {
		this(pad, UUID.randomUUID());
	}

	public ModernCartDesign(Pad pad, UUID uuid) {
		this(pad, uuid, ModernColor.GRAY1, ModernColor.RED3, ModernColor.RED2);
	}

	public ModernCartDesign(Pad pad, UUID id, ModernColor backgroundColor, ModernColor playColor, ModernColor cueInColor) {
		this.uuid = id;
		this.pad = pad;

		this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
		this.playColor = new SimpleObjectProperty<>(playColor);
		this.cueInColor = new SimpleObjectProperty<>(cueInColor);

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

	public ModernColor getCueInColor() {
		return cueInColor.get();
	}

	public void setCueInColor(ModernColor cueInColor) {
		this.cueInColor.set(cueInColor);
	}

	public ObjectProperty<ModernColor> cueInColorProperty() {
		return cueInColor;
	}

	public void addListener() {
		syncListener.addListener();
	}

	public void removeListener() {
		syncListener.removeListener();
	}

	public void reset() {
		backgroundColor.set(DEFAULT_COLOR_BACKGROUND);
		playColor.set(DEFAULT_COLOR_PLAY);
		cueInColor.set(DEFAULT_COLOR_CUE_IN);
	}

	// Color Associator
	@Override
	public Color getDesignEventColor() {
		return Color.web(playColor.get().getColorHi());
	}

	@Override
	public Color getDesignDefaultColor() {
		return Color.web(backgroundColor.get().getColorHi());
	}

	public ModernCartDesign copy(Pad pad) {
		ModernCartDesign clone = new ModernCartDesign(pad);

		clone.backgroundColor = new SimpleObjectProperty<>(getBackgroundColor());
		clone.playColor = new SimpleObjectProperty<>(getPlayColor());
		clone.cueInColor = new SimpleObjectProperty<>(getCueInColor());

		clone.pad = pad;
		clone.uuid = UUID.randomUUID();

		syncListener = new DesignUpdateListener(clone);
		if (pad.getProject().getProjectReference().isSync()) {
			CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), clone);
			clone.addListener();
		}

		return clone;
	}

	public void copyGlobalLayout(ModernGlobalDesign globalDesign) {
		setBackgroundColor(globalDesign.getBackgroundColor());
		setPlayColor(globalDesign.getPlayColor());
		setCueInColor(globalDesign.getCueInColor());
	}
}
