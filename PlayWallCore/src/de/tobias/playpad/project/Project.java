package de.tobias.playpad.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.page.PageSerializer;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.xml.XMLHandler;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
	public static final String PROJECT_NAME_PATTERN = "\\w{1}[\\w\\s-_]{0,}";
	/**
	 * Dateiendung für eine projekt Datei
	 */
	public static final String FILE_EXTENSION = ".xml";

	private final List<Page> pages;

	private final ProjectReference projectReference;
	private ProjectSettings settings;

	/**
	 * Liste mit den aktuellen Laufzeitfehlern.
	 */
	private transient ObservableList<PadException> exceptions;
	private transient IntegerProperty activePlayers;

	public Project(ProjectReference ref) {
		this.projectReference = ref;
		this.pages = new ArrayList<>();
		this.settings = new ProjectSettings();

		this.exceptions = FXCollections.observableArrayList();
		this.activePlayers = new SimpleIntegerProperty();
	}

	public ProjectSettings getSettings() {
		return settings;
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	public long getPlayedPlayers() {
		return getPads().stream().filter(p -> p.getStatus() == PadStatus.PLAY || p.getStatus() == PadStatus.PAUSE).count();
	}

	public boolean hasPlayedPlayers() {
		return getPlayedPlayers() != 0;
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
		if (pad.getPage() != index.getPage()) {
			Page oldPage = getPage(pad.getPage());
			oldPage.removePade(index.getId());
		}
		Page page = pages.get(index.getPage());
		page.setPad(index.getId(), pad);
	}

	public Collection<Pad> getPads() {
		List<Pad> pads = new ArrayList<>();
		pages.stream().map(page -> page.getPads()).forEach(pads::addAll);
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

	private static final String ROOT_ELEMENT = "Project";
	public static final String PAGE_ELEMENT = "Page";
	public static final String PAD_ELEMENT = "Pad";
	private static final String SETTINGS_ELEMENT = "Settings";

	public static Project load(ProjectReference ref, boolean loadMedia, ProfileChooseable profileChooseable)
			throws DocumentException, IOException, ProfileNotFoundException, ProjectNotFoundException, NoSuchComponentException {
		Path projectPath = ref.getProjectPath();

		if (Files.exists(projectPath)) {
			if (ref.getProfileReference() != null) {
				// Lädt das entsprechende Profile und aktiviert es
				Profile.load(ref.getProfileReference());
			} else {
				// Lädt Profile / Erstellt neues und hat es gleich im Speicher
				Profile profile = profileChooseable.getUnkownProfile();
				ref.setProfileReference(profile.getRef());
			}

			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(projectPath));
			Element rootElement = document.getRootElement();

			Project project = new Project(ref);

			// Lädt die Pages und somti auch die Pages
			XMLHandler<Page> handler = new XMLHandler<>(rootElement);
			List<Page> pages = handler.loadElements(PAGE_ELEMENT, new PageSerializer(project));
			for (Page page : pages) {
				project.pages.add(page);
			}

			// Lädt die Einstellungen
			Element settingsElement = rootElement.element(SETTINGS_ELEMENT);
			if (settingsElement != null)
				project.settings = ProjectSettings.load(settingsElement);

			// TODO Externalize, damit beim Start user feedback verbessert wird.
			for (Pad pad : project.getPads()) {
				if (loadMedia)
					pad.loadContent();
			}

			return project;
		} else {
			throw new ProjectNotFoundException(ref);
		}
	}

	public void save() throws IOException {
		Path projectPath = projectReference.getProjectPath();
		// Modules clearen und beim Speichern der pads neu setzen, damit alte Modules, die nicht gebracht werden, entfernt werden können.
		projectReference.getRequestedModules().clear();

		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement(ROOT_ELEMENT);

		// Speichern der Pads
		XMLHandler<Page> handler = new XMLHandler<>(rootElement);
		handler.saveElements(PAGE_ELEMENT, pages, new PageSerializer(this));

		// Speichern der Settings
		Element settingsElement = rootElement.addElement(SETTINGS_ELEMENT);
		settings.save(settingsElement);

		if (Files.notExists(projectPath)) {
			Files.createDirectories(projectPath.getParent());
			Files.createFile(projectPath);
		}
		XMLHandler.save(projectPath, document);
	}

	public int getActivePlayers() {
		return activePlayers.get();
	}

	public boolean hasActivePlayers() {
		return getActivePlayers() > 0;
	}

	public void increaseActivePlayers() {
		activePlayers.set(getActivePlayers() + 1);
	}

	public void dereaseActivePlayers() {
		if (activePlayers.greaterThan(0).get())
			activePlayers.set(getActivePlayers() - 1);
	}

	public ReadOnlyIntegerProperty activePlayerProperty() {
		return activePlayers;
	}

	// Exceptions
	public void addException(Pad pad, Path path, Exception exception) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> addException(pad, path, exception));
			return;
		}
		PadException padException = new PadException(pad, path, exception);
		exceptions.add(padException);
	}

	public void removeExceptions(Pad pad) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> removeExceptions(pad));
			return;
		}
		Iterator<PadException> i = exceptions.iterator();
		while (i.hasNext()) {
			PadException exception = i.next();
			if (exception.getPad().equals(pad)) {
				i.remove();
			}
		}
	}

	public void removeException(PadException exception) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> removeException(exception));
			return;
		}
		exceptions.remove(exception);
	}

	public ObservableList<PadException> getExceptions() {
		return exceptions;
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

	public void addPage() {
		int index = pages.size();
		pages.add(new Page(index, this));
	}
}
