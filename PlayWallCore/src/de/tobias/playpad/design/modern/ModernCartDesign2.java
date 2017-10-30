package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.UUID;

public class ModernCartDesign2 implements DesignColorAssociator {

	private UUID uuid;
	private ObjectProperty<ModernColor> backgroundColor;
	private ObjectProperty<ModernColor> playColor;

	private Pad pad;
	// private DesignUpdateListener syncListener; TODO Enable listener

	public ModernCartDesign2(Pad pad) {
		this(pad, UUID.randomUUID());
	}

	public ModernCartDesign2(Pad pad, UUID uuid) {
		this(pad, uuid, ModernColor.GRAY1, ModernColor.RED3);
	}

	public ModernCartDesign2(Pad pad, UUID id, ModernColor backgroundColor, ModernColor playColor) {
		this.uuid = id;
		this.pad = pad;

		this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
		this.playColor = new SimpleObjectProperty<>(playColor);

		// syncListener = new DesignUpdateListener(this);
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
//		syncListener.addListener();
	}

	public void removeListener() {
//		syncListener.removeListener();
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

	public ModernCartDesign2 clone(Pad pad) throws CloneNotSupportedException {
		ModernCartDesign2 clone = (ModernCartDesign2) super.clone();
		clone.backgroundColor = new SimpleObjectProperty<>(getBackgroundColor());
		clone.playColor = new SimpleObjectProperty<>(getPlayColor());
		clone.pad = pad;
		clone.uuid = UUID.randomUUID();

//		syncListener = new DesignUpdateListener(clone);
		if (pad.getProject().getProjectReference().isSync()) {
			addListener();
			CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), clone);
		}

		return clone;
	}

	public void copyGlobalLayout(ModernGlobalDesign2 globalDesign) {
		setBackgroundColor(globalDesign.getBackgroundColor());
		setPlayColor(globalDesign.getPlayColor());
	}
}
