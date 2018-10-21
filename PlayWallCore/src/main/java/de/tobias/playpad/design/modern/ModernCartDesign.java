package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.DesignUpdateListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.UUID;

public class ModernCartDesign implements DesignColorAssociator, Cloneable {

	private UUID uuid;
	private ObjectProperty<ModernColor> backgroundColor;
	private ObjectProperty<ModernColor> playColor;

	private Pad pad;
	private DesignUpdateListener syncListener;

	public ModernCartDesign(Pad pad) {
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

	public void addListener() {
		syncListener.addListener();
	}

	public void removeListener() {
		syncListener.removeListener();
	}

	public void reset() {
		backgroundColor.set(ModernColor.GRAY1);
		playColor.set(ModernColor.RED1);
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
			CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), clone);
			clone.addListener();
		}

		return clone;
	}

	public void copyGlobalLayout(ModernGlobalDesign globalDesign) {
		setBackgroundColor(globalDesign.getBackgroundColor());
		setPlayColor(globalDesign.getPlayColor());
	}
}
