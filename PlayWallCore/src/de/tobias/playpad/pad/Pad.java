package de.tobias.playpad.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.pad.listener.trigger.PadTriggerContentListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerDurationListener;
import de.tobias.playpad.pad.listener.trigger.PadTriggerStatusListener;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.PadUpdateListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.dom4j.Element;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * Container for Media (PadContent). It represents a slot on a page.
 *
 * @author tobias
 * @version 6.2.0
 */
public class Pad implements Cloneable {

	private UUID uuid;
	private IntegerProperty positionProperty = new SimpleIntegerProperty();
	private ObjectProperty<Page> pageProperty = new SimpleObjectProperty<>();

	private StringProperty nameProperty = new SimpleStringProperty("");
	private ObjectProperty<PadStatus> statusProperty = new SimpleObjectProperty<>(PadStatus.EMPTY);

	private SimpleStringProperty contentTypeProperty = new SimpleStringProperty();
	private ObservableList<MediaPath> mediaPaths = FXCollections.observableArrayList();

	// Content
	private ObjectProperty<PadContent> contentProperty = new SimpleObjectProperty<>();

	// Settings
	private PadSettings padSettings;

	/*
	 * Listener
	 */

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
	private transient PadUpdateListener padListener;

	public Pad(Project project) {
		this.project = project;
		this.uuid = UUID.randomUUID();
		this.padSettings = new PadSettings(this);

		initPadListener();
		// Update Trigger ist nicht notwendig, da es in load(Element) ausgerufen wird

		padListener = new PadUpdateListener(this);
	}

	public Pad(Project project, int index, Page page) {
		this(project);

		setPosition(index);
		setPage(page);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		padSettings.updateTrigger();
	}

	public Pad(Project project, int index, Page page, String name, String contentType) {
		this(project, index, page);
		setName(name);
		setContentType(contentType);
	}

	public Pad(Project project, UUID uuid, int index, Page page, String name, String contentType) {
		this(project, index, page, name, contentType);
		setUuid(uuid);
	}

	private void initPadListener() {
		// Remove old listener from properties
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

	public void addSyncListener() {
		padListener.addListener();
	}

	public void removeSyncListener() {
		padListener.removeListener();
	}

	// Accessor Methods

	/**
	 * Get the unique identifier of a pad.
	 *
	 * @return uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Set the unique identifier of a pad. Only used by the load method ({@link PadSerializer#loadElement(Element)}
	 *
	 * @param uuid uuid
	 */
	void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * et the page where the pad is.
	 *
	 * @return page
	 */
	public Page getPage() {
		return pageProperty.get();
	}

	/**
	 * Set the new page of the pad.
	 *
	 * @param page page
	 */
	public void setPage(Page page) {
		pageProperty.set(page);
	}

	/**
	 * Get the readable pad position. THis is the normal pad position + 1  {@link Pad#getPosition()}
	 *
	 * @return position
	 */
	public int getPositionReadable() {
		return positionProperty.get() + 1;
	}

	/**
	 * Get the pad position.
	 *
	 * @return position
	 */
	public int getPosition() {
		return positionProperty.get();
	}

	/**
	 * Set the new pad position.
	 *
	 * @param position position
	 */
	public void setPosition(int position) {
		this.positionProperty.set(position);
	}

	/**
	 * get the pad position property.
	 *
	 * @return position
	 */
	public ReadOnlyIntegerProperty positionProperty() {
		return positionProperty;
	}

	/**
	 * Get the pad index. This is a combination of the pad position and the page position.
	 *
	 * @return pad index
	 */
	public PadIndex getPadIndex() {
		return new PadIndex(getPosition(), getPage().getPosition());
	}

	/**
	 * Get the name of the pad.
	 *
	 * @return name
	 */
	public String getName() {
		return nameProperty.get();
	}

	/**
	 * Set the name of the page
	 *
	 * @param name name
	 */
	public void setName(String name) {
		this.nameProperty.set(name);
	}

	/**
	 * Get the name property of the pad.
	 *
	 * @return name
	 */
	public StringProperty nameProperty() {
		return nameProperty;
	}

	public Path getPath() {
		if (mediaPaths.isEmpty()) {
			return null;
		}
		return mediaPaths.get(0).getPath();
	}

	public String getFileName() {
		if (mediaPaths.isEmpty()) {
			return null;
		}
		return mediaPaths.get(0).getFileName();
	}

	public void setPath(Path path) {
		if (mediaPaths.isEmpty()) {
			createMediaPath(path);
		} else {
			setPath(path, 0);
		}
	}

	public ObservableList<MediaPath> getPaths() {
		return mediaPaths;
	}

	public void setPath(Path path, int id) {
		if (mediaPaths.size() > id && id >= 0) {
			final MediaPath mediaPath = mediaPaths.get(id);
			removePath(mediaPath);

			createMediaPath(path);
		}
	}

	public void updatePath(MediaPath mediaPath, Path localPath) {
		mediaPath.setPath(localPath, true);
	}

	private void createMediaPath(Path path) {
		final MediaPath mediaPath = MediaPath.create(this, path);

		// Sync to cloud
		addPath(mediaPath);
	}

	public void addPath(MediaPath mediaPath) {
		mediaPaths.add(mediaPath);

		if (project.getProjectReference().isSync()) {
			CommandManager.execute(Commands.PATH_ADD, project.getProjectReference(), mediaPath);
		}

		PadContent content = getContent();
		if (content != null) {
			content.loadMedia(mediaPath);
		}
	}

	public void removePath(MediaPath path) {
		mediaPaths.remove(path);

		if (project.getProjectReference().isSync()) {
			CommandManager.execute(Commands.PATH_REMOVE, project.getProjectReference(), path);
		}
	}

	/**
	 * Get the status of the pad.
	 *
	 * @return status
	 */
	public PadStatus getStatus() {
		return statusProperty.get();
	}

	/**
	 * Set the status of the pad. This controls the playback and loading functions.
	 *
	 * @param status status
	 */
	public void setStatus(PadStatus status) {
		if (status == PadStatus.NOT_FOUND)
			Thread.dumpStack();

		// Play, Pause & Stop only if the pad isn't empty
		if (status == PadStatus.PLAY || status == PadStatus.STOP || status == PadStatus.PAUSE) {
			if (this.statusProperty.get() == PadStatus.EMPTY) {
				return;
			}
		}
		// Only set pause if pad supports pause
		if (status == PadStatus.PAUSE && !(getContent() instanceof Pauseable)) {
			return;
		}
		// Don't stop the pad if it is already stopped
		if (status == PadStatus.STOP && getStatus() == PadStatus.READY) {
			return;
		}

		this.statusProperty.set(status);
	}

	/**
	 * Set the status of the pad.
	 *
	 * @param status        status
	 * @param ignoreTrigger ignore all triggers
	 */
	public void setStatus(PadStatus status, boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
		setStatus(status);
	}

	/**
	 * Get the status property.
	 *
	 * @return status
	 */
	public ObjectProperty<PadStatus> statusProperty() {
		return statusProperty;
	}

	/**
	 * Get the content of a pad.
	 *
	 * @return content
	 */
	public PadContent getContent() {
		return contentProperty.get();
	}

	/**
	 * Get the content Property of the pad.
	 *
	 * @return content
	 */
	public ReadOnlyObjectProperty<PadContent> contentProperty() {
		return contentProperty;
	}

	/**
	 * Get the content type of the pad.
	 *
	 * @return content type
	 */
	public String getContentType() {
		return contentTypeProperty.get();
	}

	/**
	 * Set the content type of the pad. It will unload the old media
	 *
	 * @param contentType content type
	 */
	public void setContentType(String contentType) throws NoSuchComponentException {
		this.contentTypeProperty.set(contentType);

		PadContent oldContent = getContent();
		if (oldContent != null) {
			oldContent.unloadMedia();
		}

		if (contentType != null) {
			PadContentFactory factory = PlayPadPlugin.getRegistryCollection().getPadContents().getFactory(contentType);
			PadContent newContent = factory.newInstance(this);
			contentProperty.set(newContent);
			newContent.loadMedia();
		} else {
			contentProperty.set(null);
		}
	}

	/**
	 * Get the content type property of the pad.
	 *
	 * @return content type
	 */
	public SimpleStringProperty contentTypeProperty() {
		return contentTypeProperty;
	}

	/**
	 * Get the pad settings.
	 *
	 * @return settings
	 */
	public PadSettings getPadSettings() {
		return padSettings;
	}

	/**
	 * Set the settings of a pad. Be careful.
	 *
	 * @param padSettings new settings
	 */
	public void setPadSettings(PadSettings padSettings) {
		this.padSettings = padSettings;
	}

	/*
	 * Utils
	 */

	/**
	 * Return true, if the media is at the end of the file.
	 *
	 * @return eof
	 */
	public boolean isEof() {
		return eof;
	}

	/**
	 * pad content could set this property to true, if the media is at the end of the file.
	 *
	 * @param eof eof
	 */
	public void setEof(boolean eof) {
		this.eof = eof;
	}

	/**
	 * Load the media of the pad into the content.
	 */
	public void loadContent() {
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
		setContentType(null);
		contentProperty.set(null);
		setStatus(PadStatus.EMPTY);

		if (project.getProjectReference().isSync()) {
			mediaPaths.forEach(path -> CommandManager.execute(Commands.PATH_REMOVE, project.getProjectReference(), path));

			CommandManager.execute(Commands.PAD_CLEAR, project.getProjectReference(), this);
		}

		mediaPaths.clear();
	}

	@Override
	public String toString() {
		return "Pad: " + positionProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (positionProperty.get() + 1) + " - " + nameProperty.get();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Pad pad = (Pad) o;

		return uuid != null ? uuid.equals(pad.uuid) : pad.uuid == null;
	}

	// Clone
	public Pad clone(Page page) throws CloneNotSupportedException {
		Pad clone = (Pad) super.clone();

		clone.uuid = UUID.randomUUID();
		clone.positionProperty = new SimpleIntegerProperty(getPosition());
		clone.pageProperty = new SimpleObjectProperty<>(getPage());
		clone.setPage(page);

		clone.nameProperty = new SimpleStringProperty(getName());
		clone.statusProperty = new SimpleObjectProperty<>(getStatus());

		clone.contentTypeProperty = new SimpleStringProperty(getContentType());
		if (getContent() != null) {
			clone.contentProperty = new SimpleObjectProperty<>(getContent().clone());
			clone.getContent().setPad(clone);
		} else {
			clone.contentProperty = new SimpleObjectProperty<>();
		}

		if (project.getProjectReference().isSync()) {
			CommandManager.execute(Commands.PAD_ADD, project.getProjectReference(), clone);
			clone.padListener = new PadUpdateListener(clone);
			clone.addSyncListener();
		}

		clone.mediaPaths = FXCollections.observableArrayList();
		for (MediaPath path : mediaPaths) {
			MediaPath clonedPath = path.clone(clone);
			clone.mediaPaths.add(clonedPath);
		}

		clone.padSettings = padSettings.clone(clone);

		clone.controller = null;
		clone.project = project;

		clone.initPadListener();
		return clone;
	}

}
