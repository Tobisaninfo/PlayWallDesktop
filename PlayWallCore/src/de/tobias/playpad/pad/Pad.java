package de.tobias.playpad.pad;

import java.nio.file.Path;
import java.util.UUID;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.listener.trigger.PadTriggerContentListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerDurationListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerStatusListener;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.volume.VolumeManager;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pad implements Cloneable {

	private static final VolumeManager volumeManager;

	static {
		volumeManager = new VolumeManager();
	}

	// Verwaltung
	private UUID uuid;
	private IntegerProperty indexProperty = new SimpleIntegerProperty();
	private IntegerProperty pageProperty = new SimpleIntegerProperty();

	private StringProperty nameProperty = new SimpleStringProperty();
	private ObjectProperty<PadStatus> statusProperty = new SimpleObjectProperty<>(PadStatus.EMPTY);

	// Content
	private ObjectProperty<PadContent> contentProperty = new SimpleObjectProperty<>();

	// Settings
	private PadSettings padSettings;

	// Global Listener (unabhängig von der UI), für Core Functions wie Play, Pause
	private transient PadStatusListener padStatusListener;

	// Trigger Listener
	private transient PadTriggerStatusListener padTriggerStatusListener;
	private transient PadTriggerDurationListener padTriggerDurationListener;
	private transient PadTriggerContentListener padTriggerContentListener;
	private transient boolean ignoreTrigger = false;

	// Utils
	private transient boolean eof;

	private transient IPadViewController controller;
	private transient Project project;

	public Pad(Project project) {
		this.project = project;
		this.uuid = UUID.randomUUID();
		this.padSettings = new PadSettings();

		initPadListener();
		// Update Trigger ist nicht notwendig, da es in load(Element) ausgerufen wird
	}

	public Pad(Project project, int index, int page) {
		this.project = project;
		this.uuid = UUID.randomUUID();
		this.padSettings = new PadSettings();

		setIndex(index);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		padSettings.updateTrigger();
	}

	public Pad(Project project, PadIndex index) {
		this(project, index.getId(), index.getPage());
	}

	public Pad(Project project, int index, int page, String name, PadContent content) {
		this(project, index, page);
		setName(name);
		setContent(content);
	}

	private void initPadListener() {
		// Remov eold listener from propeties
		if (padStatusListener != null && statusProperty != null) {
			statusProperty.removeListener(padStatusListener);
		}
		if (padTriggerStatusListener != null && statusProperty != null) {
			statusProperty.removeListener(padTriggerStatusListener);
		}
		if (padTriggerDurationListener != null && contentProperty != null) {
			contentProperty.removeListener(padTriggerContentListener);
			padTriggerContentListener.changed(contentProperty, getContent(), null);
		}

		// init new listener for properties
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

	public UUID getUuid() {
		return uuid;
	}

	void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public int getPage() {
		return pageProperty.get();
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

	public void setPage(int page) {
		pageProperty.set(page);
	}

	public PadIndex getPadIndex() {
		return new PadIndex(getIndex(), getPage());
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

	public IPadViewController getController() {
		return controller;
	}

	public void setController(IPadViewController controller) {
		this.controller = controller;
	}

	public void clear() {
		setName("");
		if (contentProperty.isNotNull().get())
			contentProperty.get().unloadMedia();
		setContent(null);
		setStatus(PadStatus.EMPTY);

		if (project != null) {
			// TODO Remove Exceptions refer to pad
		}
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

	@Override
	public String toString() {
		return "Pad: " + indexProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (indexProperty.get() + 1) + " - " + nameProperty.get();
	}

	// Volume Manager
	public static VolumeManager getVolumeManager() {
		return volumeManager;
	}

	// Clone
	@Override
	public Pad clone() throws CloneNotSupportedException {
		Pad clone = (Pad) super.clone();

		clone.uuid = UUID.randomUUID();
		clone.indexProperty = new SimpleIntegerProperty();
		clone.pageProperty = new SimpleIntegerProperty();

		clone.nameProperty = new SimpleStringProperty(getName());
		clone.statusProperty = new SimpleObjectProperty<PadStatus>(getStatus());
		clone.contentProperty = new SimpleObjectProperty<PadContent>(getContent().clone());
		clone.getContent().setPad(clone);
		
		clone.padSettings = padSettings.clone();

		clone.initPadListener();
		return clone;
	}
}
