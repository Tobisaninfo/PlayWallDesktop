package de.tobias.playpad.project.page;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.api.IPage;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.PageUpdateListener;
import javafx.beans.property.*;

import java.util.*;

/**
 * Manage a page in the project and the pads inside the page.
 *
 * @author tobias
 * @since 6.0.0
 */
public class Page implements IPage {

	private UUID id;
	private IntegerProperty positionProperty;
	private StringProperty nameProperty;
	private Set<Pad> pads;

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
		this.pads = new HashSet<>();

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
		pads.forEach(Pad::removeSyncListener);
	}

	/**
	 * Return the page id.
	 *
	 * @return id
	 */
	@Override
	public UUID getId() {
		return id;
	}

	/**
	 * Get the position of the page.
	 *
	 * @return page position.
	 */
	@Override
	public int getPosition() {
		return positionProperty.get();
	}

	/**
	 * Set the new page position.
	 *
	 * @param position position.
	 */
	public void setPosition(int position) {
		if (position < 0)
			return;

		this.positionProperty.set(position);
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
	@Override
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
	 * @param position index
	 * @return pad
	 * @throws IllegalArgumentException bad index
	 */
	public Pad getPad(int position) throws IllegalArgumentException {
		ProjectSettings settings = projectReference.getSettings();
		int maxId = settings.getRows() * settings.getColumns();
		if (position < 0 || position > maxId) {
			throw new IllegalArgumentException("Illegal index: index is " + position + " but it must in a range of 0 to " + maxId);
		}

		if (pads.stream().noneMatch(p -> p.getPosition() == position)) {
			// Create new pad for positionProperty
			Pad pad = new Pad(projectReference, position, this);
			setPad(position, pad);

			if (projectReference.getProjectReference().isSync()) {
				CommandManager.execute(Commands.PAD_ADD, projectReference.getProjectReference(), pad);
				CommandManager.execute(Commands.PAD_SETTINGS_ADD, projectReference.getProjectReference(), pad.getPadSettings());
			}
		}
		Optional<Pad> padOptional = pads.stream().filter(p -> p.getPosition() == position).findFirst();
		return padOptional.orElse(null);
	}

	/**
	 * Get the right pad for this coordinates. If the coordinates are wrong the method will return null
	 *
	 * @param x x position
	 * @param y y position
	 * @return pad
	 */
	@Override
	public Pad getPad(int x, int y) {
		ProjectSettings settings = projectReference.getSettings();
		if (x < settings.getColumns() && y < settings.getRows()) {
			int position = getPadPosition(x, y, settings);
			return getPad(position);
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
		pads.removeIf(p -> p.getPosition() == id);
		if (pad != null) {
			pads.add(pad);
			pad.setPage(this);
			pad.setPosition(id);

			if (projectReference.getProjectReference().isSync()) {
				pad.addSyncListener();
			}
		}
	}

	/**
	 * Get a list of all pads on a page. (Read only)
	 *
	 * @return pads
	 */
	@Override
	public Collection<Pad> getPads() {
		return Collections.unmodifiableCollection(pads);
	}

	/**
	 * Removes a pad from a page and from the cloud.
	 *
	 * @param uuid         id of the pad
	 * @param deleteRemote <code>true</code> delete from remote
	 */
	public void removePad(UUID uuid, boolean deleteRemote) {
		if (projectReference.getProjectReference().isSync() && deleteRemote) {
			Optional<Pad> padOptional = pads.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();
			Pad temp = padOptional.orElse(null);
			if (temp != null) {
				temp.removeSyncListener();
				CommandManager.execute(Commands.PAD_REMOVE, projectReference.getProjectReference(), temp);
			}
		}
		pads.removeIf(p -> p.getUuid().equals(uuid));
	}

	public void addColumn() {
		final int columns = getProject().getSettings().getColumns() - 1;
		final int rows = getProject().getSettings().getRows();

		for (int i = 0; i < rows; i++) {
			insertPadAndShiftSuccessor(columns, i, rows * columns);
		}
	}

	public void addRow() {
		final int columns = getProject().getSettings().getColumns();
		final int rows = getProject().getSettings().getRows() - 1;

		for (int i = 0; i < columns; i++) {
			insertPadAndShiftSuccessor(columns, i, rows * columns);
		}
	}

	/**
	 * Insert a pad into given (x, y). Moves all other pads one position ahead.
	 *
	 * @param x            x
	 * @param y            y
	 * @param lastPadIndex index of the last pad on the page
	 */
	private void insertPadAndShiftSuccessor(int x, int y, int lastPadIndex) {
		int position = getPadPosition(x, y, getProject().getSettings());
		// Going backwards to avoid overwriting the next pad when updating the position of the current one.
		for (int i = lastPadIndex - 1; i >= position; i--) {
			getPad(i).setPosition(i + 1);
		}
		getPad(position);
	}

	@Override
	public String toString() {
		return "Page [positionProperty=" + positionProperty + "]";
	}

	public Page copy() {
		Page clone = new Page(getPosition(), projectReference);
		clone.id = UUID.randomUUID();
		clone.positionProperty = new SimpleIntegerProperty(getPosition());
		clone.nameProperty = new SimpleStringProperty(getName());
		clone.projectReference = projectReference;

		if (projectReference.getProjectReference().isSync()) {
			CommandManager.execute(Commands.PAGE_ADD, projectReference.getProjectReference(), clone);
			clone.syncListener = new PageUpdateListener(clone);
			clone.syncListener.addListener();
		}

		clone.pads = new HashSet<>();
		for (Pad pad : pads) {
			Pad padClone = pad.copy(clone);
			clone.pads.add(padClone);
		}
		return clone;
	}
}
