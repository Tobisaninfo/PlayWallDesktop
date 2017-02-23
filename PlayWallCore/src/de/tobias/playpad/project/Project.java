package de.tobias.playpad.project;

import de.tobias.playpad.pad.MediaPath;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.sync.command.page.PageAddCommand;
import de.tobias.playpad.server.sync.command.page.PageRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectAddCommand;
import de.tobias.playpad.server.sync.listener.upstream.ProjectUpdateListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Verwaltet alle Seiten, die jeweils die Kacheln enthalten.
 *
 * @author tobias
 * @since 6.0.0
 */
public class Project {

	/**
	 * Pattern für den Namen des Projekts
	 */
	public static final String PROJECT_NAME_PATTERN = "[\\p{L}0-9]{1}[\\p{L}\\s-_0-9]{0,}";

	/**
	 * Dateiendung für eine projekt Datei
	 */
	public static final String FILE_EXTENSION = ".xml";

	final ObservableList<Page> pages;

	ProjectSettings settings;
	final ProjectReference projectReference;

	private transient IntegerProperty activePlayerProperty;
	private transient ProjectUpdateListener syncListener;

	public Project(ProjectReference ref) {
		this.projectReference = ref;
		this.pages = FXCollections.observableArrayList();
		this.settings = new ProjectSettings();
		this.activePlayerProperty = new SimpleIntegerProperty();

		syncListener = new ProjectUpdateListener(this);
		if (ref.isSync()) {
			syncListener.addListener();
		}
	}

	public void close() {
		syncListener.removeListener();
	}

	public ProjectSettings getSettings() {
		return settings;
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	public Pad getPad(int x, int y, int page) {
		return getPage(page).getPad(x, y);
	}

	public Pad getPad(PadIndex index) {
		Page page = pages.get(index.getPagePosition());
		return page.getPad(index.getId());
	}

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
		if (pad != null) {
			// Remove Pad from old location
			if (pad.getPage().getPosition() != index.getPagePosition()) {
				Page oldPage = pad.getPage();
				if (oldPage.getPad(pad.getPosition()).equals(pad)) {
					oldPage.setPad(index.getId(), null);
				}
			}
		}
		Page page = pages.get(index.getPagePosition());
		page.setPad(index.getId(), pad);
	}

	public Collection<Pad> getPads() {
		List<Pad> pads = new ArrayList<>();
		pages.stream().map(Page::getPads).forEach(pads::addAll);
		return pads;
	}

	public void removePad(PadIndex index) {
		Page page = getPage(index.getPagePosition());
		page.removePad(index.getId(), true);
	}

	// Pages
	public Page getPage(int psotion) {
		if (psotion >= pages.size() && psotion < ProjectSettings.MAX_PAGES) {
			addPage(new Page(psotion, this));
		}
		return pages.get(psotion);
	}


	public Page getPage(UUID uuid) {
		for (Page page : pages) {
			if (page.getId().equals(uuid)) {
				return page;
			}
		}
		return null;
	}

	public ObservableList<Page> getPages() {
		// Create new page if all is empty (automatic)
		if (pages.isEmpty()) {
			addPage(new Page(0, this));
		}
		return pages;
	}

	public void setPage(int index, Page page) {
		if (pages.contains(page))
			pages.remove(page);

		pages.add(index, page);
		page.setPosition(index);
	}

	public int getActivePlayers() {
		return (int) getPads().stream().filter(p -> p.getStatus() == PadStatus.PLAY || p.getStatus() == PadStatus.PAUSE).count();
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

	// Utils
	public void loadPadsContent() {
		getPads().forEach(Pad::loadContent);
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
			PageRemoveCommand.removePage(page);

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
			PageAddCommand.addPage(page);
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
			if (pad.getStatus() != PadStatus.EMPTY) {
				if (pad.getName().toLowerCase().contains(name.toLowerCase())) {
					result.add(pad);
				}
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
}
