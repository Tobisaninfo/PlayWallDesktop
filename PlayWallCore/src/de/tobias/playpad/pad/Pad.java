package de.tobias.playpad.pad;

import java.nio.file.Path;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.listener.trigger.PadTriggerContentListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerDurationListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerStatusListener;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pad {

	// Verwaltung
	private IntegerProperty indexProperty = new SimpleIntegerProperty();
	private StringProperty nameProperty = new SimpleStringProperty();
	private ObjectProperty<PadStatus> statusProperty = new SimpleObjectProperty<>(PadStatus.EMPTY);

	// Content
	private ObjectProperty<PadContent> contentProperty = new SimpleObjectProperty<>();

	// Settings
	private PadSettings padSettings;

	// Custom Volume
	private transient DoubleProperty customVolumeProperty = new SimpleDoubleProperty(1.0);

	// Global Listener (unabhängig von der UI), für Core Functions wie Play, Pause
	private transient PadStatusListener padStatusListener;

	// Trigger Listener
	private transient PadTriggerStatusListener padTriggerStatusListener;
	private transient PadTriggerDurationListener padTriggerDurationListener;
	private transient PadTriggerContentListener padTriggerContentListener;
	private transient boolean ignoreTrigger = false;

	// Utils
	private transient boolean eof;
	private transient IPadViewControllerV2 controller;
	private transient Project project;

	public Pad(Project project) {
		this.project = project;
		padSettings = new PadSettings();

		initPadListener();
		// Update Trigger ist nicht notwendig, da es in load(Element) ausgerufen wird
	}

	public Pad(Project project, int index) {
		this.project = project;
		padSettings = new PadSettings();

		setIndex(index);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		padSettings.updateTrigger();
	}

	public Pad(Project project, int index, String name, PadContent content) {
		this(project, index);
		setName(name);
		setContent(content);
	}

	private void initPadListener() {
		padStatusListener = new PadStatusListener(this);
		statusProperty.addListener(padStatusListener);

		padTriggerStatusListener = new PadTriggerStatusListener(this);
		statusProperty.addListener(padTriggerStatusListener);

		padTriggerDurationListener = new PadTriggerDurationListener(this);

		// Das ist für die Position Listener notwendig, wenn sich der Content ändert
		padTriggerContentListener = new PadTriggerContentListener(this);
		contentProperty.addListener(padTriggerContentListener);
		padTriggerContentListener.changed(contentProperty, null, getContent());
	}

	// Accessor Methods
	public int getIndex() {
		return indexProperty.get();
	}

	public int getIndexReadable() {
		return indexProperty.get() + 1;
	}

	public void setIndex(int index) {
		this.indexProperty.set(index);
	}

	public ReadOnlyIntegerProperty indexProperty() {
		return indexProperty;
	}

	public String getName() {
		return nameProperty.get();
	}

	public void setName(String name) {
		this.nameProperty.set(name);
	}

	public StringProperty nameProperty() {
		return nameProperty;
	}

	public PadStatus getStatus() {
		return statusProperty.get();
	}

	public void setStatus(PadStatus status) {
		// PLay, Pause & Stop nut wenn Pad Content hat
		if (status == PadStatus.PLAY || status == PadStatus.STOP || status == PadStatus.PAUSE) {
			if (this.statusProperty.get() == PadStatus.EMPTY) {
				return;
			}
		}
		// Pause nur wenn Pause möglich
		if (status == PadStatus.PAUSE && !(getContent() instanceof Pauseable)) {
			return;
		}
		// Stop nicht wenn Ready (Stop/Keine Wiedergabe)
		if (status == PadStatus.STOP && getStatus() == PadStatus.READY) {
			return;
		}

		this.statusProperty.set(status);
	}

	public void setStatus(PadStatus status, boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
		setStatus(status);
	}

	public ObjectProperty<PadStatus> statusProperty() {
		return statusProperty;
	}

	public PadContent getContent() {
		return contentProperty.get();
	}

	public void setContent(PadContent content) {
		this.contentProperty.set(content);
	}

	public ObjectProperty<PadContent> contentProperty() {
		return contentProperty;
	}

	public PadSettings getPadSettings() {
		return padSettings;
	}

	public void setMasterVolume(double volume) {
		if (getContent() != null) {
			getContent().setMasterVolume(volume);
		}
	}

	public boolean isEof() {
		return eof;
	}

	public void setEof(boolean eof) {
		this.eof = eof;
	}

	// Helper Methodes
	public void loadContent() throws NoSuchComponentException {
		if (contentProperty.get() != null)
			contentProperty.get().loadMedia();
	}

	public void throwException(Path path, Exception exception) {
		if (project != null)
			project.addException(this, path, exception);
		setStatus(PadStatus.ERROR);
	}

	public void removeExceptionsForPad() {
		if (project != null)
			project.removeExceptions(this);
	}

	public void removeException(PadException exception) {
		if (project != null)
			project.removeException(exception);
	}

	public PadTriggerDurationListener getPadTriggerDurationListener() {
		return padTriggerDurationListener;
	}

	public boolean isIgnoreTrigger() {
		return ignoreTrigger;
	}

	public void setIgnoreTrigger(boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
	}

	public Project getProject() {
		return project;
	}

	public boolean isPadVisible() {
		return controller != null;
	}

	public IPadViewControllerV2 getController() {
		return controller;
	}

	public void setController(IPadViewControllerV2 controller) {
		this.controller = controller;
	}

	public void clear() {
		setName("");
		if (contentProperty.isNotNull().get())
			contentProperty.get().unloadMedia();
		setContent(null);
		setStatus(PadStatus.EMPTY);

		if (project != null) {
			project.removeExceptions(this);
		}
	}

	@Override
	public String toString() {
		return "Pad: " + indexProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (indexProperty.get() + 1) + " - " + nameProperty.get();
	}

	// TODO Reorder
	public void setCustomVolume(double volume) {
		customVolumeProperty.set(volume);
	}

	public double getCustomVolume() {
		return customVolumeProperty.get();
	}

	public DoubleProperty customVolumeProperty() {
		return customVolumeProperty;
	}
}
