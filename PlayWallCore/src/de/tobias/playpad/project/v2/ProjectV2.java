package de.tobias.playpad.project.v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.ProfileChooseable;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.page.PageSerializer;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.xml.XMLHandler;

/**
 * Verwaltet alle Seiten, die jeweils die Kacheln enthalten.
 * 
 * @author tobias
 *
 * @since 6.0.0
 */
public class ProjectV2 {

	/**
	 * Pattern für den Namen des Projekts
	 */
	public static final String PROJECT_NAME_PATTERN = "\\w{1}[\\w\\s-_]{0,}";
	/**
	 * Dateiendung für eine projekt Datei
	 */
	public static final String FILE_EXTENSION = ".xml";

	private final HashMap<Integer, Page> pages;

	private final ProjectReference projectReference;
	private ProjectSettings settings;

	public ProjectV2(ProjectReference ref) {
		this.projectReference = ref;
		this.pages = new HashMap<>();
		this.settings = new ProjectSettings();
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

	public void setPad(PadIndex index, Pad pad) {
		Page page = pages.get(index.getPage());
		page.setPad(index.getId(), pad);
	}

	public Collection<Pad> getPads() {
		List<Pad> pads = new ArrayList<>();
		pages.values().stream().map(page -> page.getPads()).forEach(pads::addAll);
		return pads;
	}

	public Page getPage(int index) {
		if (!pages.containsKey(index) && index < settings.getPageCount()) {
			pages.put(index, new Page(index, this));
		}
		return pages.get(index);
	}

	public Collection<Page> getPages() {
		return pages.values();
	}

	private static final String ROOT_ELEMENT = "Project";
	public static final String PAGE_ELEMENT = "Page";
	public static final String PAD_ELEMENT = "Pad";
	private static final String SETTINGS_ELEMENT = "Settings";

	public static ProjectV2 load(ProjectReference ref, boolean loadMedia, ProfileChooseable profileChooseable)
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

			ProjectV2 project = new ProjectV2(ref);

			// Lädt die Pages und somti auch die Pages
			XMLHandler<Page> handler = new XMLHandler<>(rootElement);
			List<Page> pages = handler.loadElements(PAGE_ELEMENT, new PageSerializer(project));
			for (Page page : pages) {
				project.pages.put(page.getId(), page);
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
		Document document = DocumentHelper.createDocument();

		Element rootElement = document.addElement(ROOT_ELEMENT);

		// Speichern der Pads
		XMLHandler<Page> handler = new XMLHandler<>(rootElement);
		handler.saveElements(PAGE_ELEMENT, pages.values(), new PageSerializer());

		// Speichern der Settings
		Element settingsElement = rootElement.addElement(SETTINGS_ELEMENT);
		settings.save(settingsElement);

		if (Files.notExists(projectPath)) {
			Files.createDirectories(projectPath.getParent());
			Files.createFile(projectPath);
		}
		XMLHandler.save(projectPath, document);
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

}
