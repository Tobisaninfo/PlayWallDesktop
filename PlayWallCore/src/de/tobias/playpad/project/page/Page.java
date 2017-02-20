package de.tobias.playpad.project.page;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.server.sync.listener.upstream.PageUpdateListener;
import javafx.beans.property.*;

/**
 * Manage a page in the project and the pads inside the page.
 *
 * @author tobias
 * @since 6.0.0
 */
public class Page implements Cloneable {

	private final UUID id;
	private IntegerProperty positionProperty;
	private StringProperty nameProperty;
	private HashMap<Integer, Pad> pads;

	private transient Project projectReference;
	private transient PageUpdateListener syncListener;

	/**
	 * Create a page with position
	 *
	 * @param position position
	 * @param project  referenced project
	 */
	public Page(int position, Project project) {
		this(position, "", project);
	}

	/**
	 * Create a page with position and name
	 *
	 * @param position position
	 * @param name     name
	 * @param project  referenced project
	 */
	public Page(int position, String name, Project project) {
		this(UUID.randomUUID(), position, name, project);
	}

	/**
	 * Create a page with UUID, position and name
	 *
	 * @param id       id
	 * @param position position
	 * @param name     name
	 * @param project  referenced project
	 */
	public Page(UUID id, int position, String name, Project project) {
		this.id = id;
		this.positionProperty = new SimpleIntegerProperty(position);
		this.nameProperty = new SimpleStringProperty(name);
		this.pads = new HashMap<>();

		this.projectReference = project;

		this.syncListener = new PageUpdateListener(this);
		if (project.getProjectReference().isSync()) {
			syncListener.addListener();
		}
	}

	// Sync listener

	/**
	 * Remove the sync listener from the page
	 */
	public void removeSyncListener() {
		syncListener.removeListener();
	}

	/**
	 * Return the page id.
	 *
	 * @return id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Get the position of the page.
	 *
	 * @return page position.
	 */
	public int getPosition() {
		return positionProperty.get();
	}

	/**
	 * Set the new page position.
	 *
	 * @param position position.
	 */
	public void setPosition(int position) {
		if (position < 0) {
			return;
		}

		this.positionProperty.set(position);
		for (Pad pad : pads.values()) {
			pad.setPage(position);
		}
	}

	/**
	 * Get the position property (read only).
	 *
	 * @return position property
	 */
	public ReadOnlyIntegerProperty positionProperty() {
		return positionProperty;
	}

	/**
	 * Get the name of the page.
	 *
	 * @return name
	 */
	public String getName() {
		return nameProperty.get();
	}

	/**
	 * Set the name of the page.
	 *
	 * @param name new name
	 */
	public void setName(String name) {
		this.nameProperty.set(name);
	}

	/**
	 * Get the name property of the page.
	 *
	 * @return name property
	 */
	public StringProperty nameProperty() {
		return nameProperty;
	}

	/**
	 * Get the project.
	 *
	 * @return project
	 */
	public Project getProject() {
		return projectReference;
	}

	/**
	 * Get the pad at this index. It will create a new pad at this index if this index is empty. In case of a wrong index the method will throw an exception.
	 *
	 * @param id index
	 * @return pad
	 * @throws IllegalArgumentException bad index
	 */
	public Pad getPad(int id) throws IllegalArgumentException {
		ProjectSettings settings = projectReference.getSettings();
		int maxId = settings.getRows() * settings.getColumns();
		if (id < 0 || id > maxId) {
			throw new IllegalArgumentException("Illegal index: index is " + id + " but it must in a range of 0 to " + maxId);
		}

		if (!pads.containsKey(id)) {
			// Create new pad for positionProperty
			setPad(id, new Pad(projectReference, id, getPosition()));
		}
		return pads.get(id);
	}

	/**
	 * Get the right pad for this coordinates. If the coordinates are wrong the method will return null
	 *
	 * @param x x position
	 * @param y y position
	 * @return pad
	 */
	public Pad getPad(int x, int y) {
		ProjectSettings settings = projectReference.getSettings();
		if (x < settings.getColumns() && y < settings.getRows()) {
			int id = y * settings.getColumns() + x;
			return getPad(id);
		}
		return null;
	}

	/**
	 * Set a pad to a new id. It overwrites the old pad. If the pad argument is null, it only removes the old pad.
	 *
	 * @param id  id
	 * @param pad pad
	 */
	public void setPad(int id, Pad pad) {
		if (pad == null) {
			pads.remove(id);
		} else {
			pads.put(id, pad);
			pad.setPage(getPosition());
			pad.setIndex(id);
		}
	}

	/**
	 * Get a list of all pads on a page. (Read only)
	 *
	 * @return pads
	 */
	public Collection<Pad> getPads() {
		return Collections.unmodifiableCollection(pads.values());
	}

	/**
	 * Removes a pad from a page.
	 *
	 * @param id index of the pad
	 */
	public void removePad(int id) {
		pads.remove(id);
	}

	@Override
	public String toString() {
		return "Page [positionProperty=" + positionProperty + "]";
	}

	@Override
	public Page clone() throws CloneNotSupportedException {
		Page clone = (Page) super.clone();
		clone.positionProperty = positionProperty;
		clone.nameProperty = nameProperty;
		clone.projectReference = projectReference;
		clone.pads = new HashMap<>();
		for (int key : pads.keySet()) {
			Pad padClone = pads.get(key).clone();
			clone.pads.put(key, padClone);
		}
		return clone;
	}
}
