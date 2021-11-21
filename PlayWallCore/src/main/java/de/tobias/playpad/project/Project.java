package de.tobias.playpad.project;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.api.IProject;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.ProjectUpdateListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Verwaltet alle Seiten, die jeweils die Kacheln enthalten.
 *
 * @author tobias
 * @since 6.0.0
 */
public class Project implements IProject {

	/**
	 * Project file extension.
	 */
	public static final String FILE_EXTENSION = ".xml";

	/**
	 * List of pages in the project.
	 */
	final ObservableList<Page> pages;

	/**
	 * Project Settings.
	 */
	ProjectSettings settings;
	/**
	 * Project Metadata.
	 */
	final ProjectReference projectReference;

	private final transient IntegerProperty activePlayerProperty;
	private final transient IntegerProperty notFoundMediaProperty;
	private final transient ProjectUpdateListener syncListener;

	public Project(ProjectReference ref) {
		this.projectReference = ref;
		this.pages = FXCollections.observableArrayList();
		this.settings = new ProjectSettings();
		this.activePlayerProperty = new SimpleIntegerProperty();
		this.notFoundMediaProperty = new SimpleIntegerProperty();

		syncListener = new ProjectUpdateListener(this);
		if (ref.isSync()) {
			syncListener.addListener();
		}
	}

	public void close() {
		syncListener.removeListener();

		getPads().parallelStream()
				.filter(pad -> pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE)
				.forEach(Pad::stop);
	}

	@Override
	public ProjectSettings getSettings() {
		return settings;
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	@Override
	public Pad getPad(int x, int y, int page) {
		return getPage(page).getPad(x, y);
	}

	@Override
	public Pad getPad(PadIndex index) {
		Page page = pages.get(index.getPagePosition());
		return page.getPad(index.getId());
	}

	@Override
	public Pad getPad(UUID uuid) {
		for (Page page : pages) {
			for (Pad pad : page.getPads()) {
				if (pad.getUuid().equals(uuid)) {
					return pad;
				}
			}
		}
		return null;
	}

	public void setPad(PadIndex index, Pad pad) {
		if (pad == null) {
			return;
		}
		// Remove Pad from old location
		if (pad.getPage().getPosition() != index.getPagePosition()) {
			Page oldPage = pad.getPage();
			if (oldPage.getPad(pad.getPosition()).equals(pad)) {
				oldPage.setPad(index.getId(), null);
			}
		}

		Page page = pages.get(index.getPagePosition());
		page.setPad(index.getId(), pad);
	}

	@Override
	public Collection<Pad> getPads() {
		return getPads(p -> true);
	}

	public Collection<Pad> getPads(Predicate<Pad> predicate) {
		return pages.parallelStream()
				.flatMap(p -> p.getPads().stream())
				.filter(predicate)
				.collect(Collectors.toList());
	}

	public void removePad(UUID id) {
		Pad pad = getPad(id);
		pad.clear();
		pad.getPage().removePad(id, true);
	}

	// Pages
	@Override
	public Page getPage(int position) {
		if (position >= ProjectSettings.MAX_PAGES) {
			return null;
		}

		while (position >= pages.size()) {
			addPage(new Page(position, this));
		}
		return pages.get(position);
	}

	@Override
	public Page getPage(UUID uuid) {
		for (Page page : pages) {
			if (page.getId().equals(uuid)) {
				return page;
			}
		}
		return null;
	}

	@Override
	public ObservableList<Page> getPages() {
		// Create new page if all is empty (automatic)
		if (pages.isEmpty()) {
			addPage(new Page(0, this));
		}
		return pages;
	}

	public void setPage(int index, Page page) {
		pages.remove(page);

		pages.add(index, page);
		page.setPosition(index);
	}

	public int getActivePlayers() {
		return getPads(p -> p.getStatus() == PadStatus.PLAY || p.getStatus() == PadStatus.PAUSE).size();
	}

	public boolean hasActivePlayers() {
		return getActivePlayers() > 0;
	}

	public IntegerProperty activePlayerProperty() {
		return activePlayerProperty;
	}

	public void updateActivePlayerProperty() {
		activePlayerProperty.set(getActivePlayers());
	}

	public int getNotFoundMedia() {
		return notFoundMediaProperty.get();
	}

	public IntegerProperty notFoundMediaProperty() {
		return notFoundMediaProperty;
	}

	public void updateNotFoundProperty() {
		notFoundMediaProperty.set(getPads(p -> p.getStatus() == PadStatus.NOT_FOUND).size());
	}

	@Override
	public String toString() {
		return projectReference.getName() + " (" + projectReference.getUuid() + ")";
	}

	public void closeFile() {
		getPads().forEach(pad ->
		{
			if (pad.getContent() != null)
				pad.getContent().unloadMedia();
		});
	}

	public void removePage(Page page) {
		if (projectReference.isSync()) {
			// Remove remote new page
			CommandManager.execute(Commands.PAGE_REMOVE, projectReference, page);

			// Remove sync listener
			page.removeSyncListener();
		}

		pages.remove(page.getPosition());
		// Reindex all pages
		for (int i = page.getPosition(); i < pages.size(); i++) {
			Page tempPage = pages.get(i);
			tempPage.setPosition(i);
		}
	}

	public boolean addPage() {
		int index = pages.size();
		Page page = new Page(index, this);
		return addPage(page);
	}

	public boolean addPage(Page page) {
		if (pages.size() == ProjectSettings.MAX_PAGES) {
			return false;
		}

		int newIndex = pages.size();

		page.setPosition(newIndex);

		if (projectReference.isSync()) {
			// Add remote new page
			CommandManager.execute(Commands.PAGE_ADD, projectReference, page);
		}

		pages.add(page);

		return true;
	}

	/**
	 * Find pads, which name starts with a given string
	 *
	 * @param name search key
	 * @return found pads in project
	 */
	public List<Pad> findPads(String name) {
		List<Pad> result = new ArrayList<>();
		for (Pad pad : getPads()) {
			if (pad.getStatus() != PadStatus.EMPTY && pad.getName().toLowerCase().contains(name.toLowerCase())) {
				result.add(pad);
			}
		}
		return result;
	}

	public MediaPath getMediaPath(UUID uuid) {
		for (Pad pad : getPads()) {
			for (MediaPath path : pad.getPaths()) {
				if (path.getId().equals(uuid)) {
					return path;
				}
			}
		}
		return null;
	}

	public void addColumn() {
		settings.setColumns(settings.getColumns() + 1);
		pages.forEach(Page::addColumn);
	}

	public void addRow() {
		settings.setRows(settings.getRows() + 1);
		pages.forEach(Page::addRow);
	}
}
