package de.tobias.playpad.project;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.server.sync.command.project.ProjectAddCommand;
import de.tobias.playpad.server.sync.listener.upstream.ProjectUpdateListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Verwaltet alle Seiten, die jeweils die Kacheln enthalten.
 * 
 * @author tobias
 *
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

	final List<Page> pages;

	ProjectSettings settings;
	final ProjectReference projectReference;

	private transient IntegerProperty activePlayerProperty;
	private transient ProjectUpdateListener syncListener;

	Project(ProjectReference ref) {
		this.projectReference = ref;
		this.pages = new ArrayList<>();
		this.settings = new ProjectSettings();
		this.activePlayerProperty = new SimpleIntegerProperty();

		if (ref.isSync()) {
			syncListener = new ProjectUpdateListener(this);
		}
	}

	public static Project create(String name, ProfileReference reference, boolean sync) throws IOException {
		ProjectReference ref = new ProjectReference(UUID.randomUUID(), name, reference, sync);
		Project project = new Project(ref);

		// Save To Disk
		ProjectSerializer.save(project);

		// Save To Cloud
		if (ref.isSync()) {
			ProjectAddCommand.addProject(project);
		}

		// Add to Project List
		ProjectReferences.addProject(ref);

		return project;
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
		Page page = pages.get(index.getPage());
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
			if (pad.getPage() != index.getPage()) {
				Page oldPage = getPage(pad.getPage());
				if (oldPage.getPad(pad.getIndex()).equals(pad)) {
					oldPage.removePade(index.getId());
				}
			}
		}
		Page page = pages.get(index.getPage());
		page.setPad(index.getId(), pad);
	}

	public Collection<Pad> getPads() {
		List<Pad> pads = new ArrayList<>();
		pages.stream().map(Page::getPads).forEach(pads::addAll);
		return pads;
	}

	// Pages
	public Page getPage(int index) {
		if (index >= pages.size() && index < ProjectSettings.MAX_PAGES) {
			pages.add(new Page(index, this));
		}
		return pages.get(index);
	}

	public Collection<Page> getPages() {
		// Create new page if all is empty (automaticlly)
		if (pages.isEmpty()) {
			pages.add(new Page(0, this));
		}
		return pages;
	}

	public void setPage(int index, Page page) {
		pages.set(index, page);
		page.setId(index);
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
		getPads().forEach(pad ->
		{
			try {
				pad.loadContent();
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
				// TODO handle exception withon project
			}
		});
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
		pages.remove(page.getId());
		// Neue Interne Indies für die Pages
		for (int i = page.getId(); i < pages.size(); i++) {
			Page tempPage = pages.get(i);
			tempPage.setId(i);
		}
	}

	public boolean addPage() {
		int index = pages.size();
		return addPage(new Page(index, this));
	}

	public boolean addPage(Page page) {
		if (pages.size() == ProjectSettings.MAX_PAGES) {
			return false;
		}

		int newIndex = pages.size();

		page.setId(newIndex);
		pages.add(page);

		return true;
	}

	/**
	 * Find pads, which name starts with a given string
	 * 
	 * @param name
	 *            search key
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
}
