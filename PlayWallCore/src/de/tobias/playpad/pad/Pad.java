package de.tobias.playpad.pad;

import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.pad.listener.trigger.PadTriggerContentListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerDurationListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerStatusListener;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.server.sync.command.pad.PadAddCommand;
import javafx.beans.property.*;

import java.util.UUID;

/**
 * Container for Media (PadContent). It represents a slot on a page.
 */
public class Pad implements Cloneable {

	// Verwaltung
	private UUID uuid;
	private IntegerProperty positionProperty = new SimpleIntegerProperty();
	private IntegerProperty pageProperty = new SimpleIntegerProperty();

	private StringProperty nameProperty = new SimpleStringProperty("");
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

		setPosition(index);
		setPage(page);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		padSettings.updateTrigger();

		if (project.getProjectReference().isSync()) {
			PadAddCommand.addPad(this);
		}
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
	public int getPosition() {
		return positionProperty.get();
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
		return positionProperty.get() + 1;
	}

	public void setPosition(int position) {
		this.positionProperty.set(position);
	}

	public ReadOnlyIntegerProperty positionProperty() {
		return positionProperty;
	}

	public void setPage(int page) {
		pageProperty.set(page);
	}

	public PadIndex getPadIndex() {
		return new PadIndex(getPosition(), getPage());
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
		// Only set pause if pad supports pause
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
	}

	@Override
	public String toString() {
		return "Pad: " + positionProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (positionProperty.get() + 1) + " - " + nameProperty.get();
	}

	// Clone
	@Override
	public Pad clone() throws CloneNotSupportedException {
		Pad clone = (Pad) super.clone();

		clone.uuid = UUID.randomUUID();
		clone.positionProperty = new SimpleIntegerProperty(getPosition());
		clone.pageProperty = new SimpleIntegerProperty(getPage());

		clone.nameProperty = new SimpleStringProperty(getName());
		clone.statusProperty = new SimpleObjectProperty<>(getStatus());
		if (getContent() != null) {
			clone.contentProperty = new SimpleObjectProperty<>(getContent().clone());
			clone.getContent().setPad(clone);
		} else {
			clone.contentProperty = new SimpleObjectProperty<>();
		}

		clone.padSettings = padSettings.clone();

		clone.controller = null;
		clone.project = project;

		clone.initPadListener();
		return clone;
	}
}
