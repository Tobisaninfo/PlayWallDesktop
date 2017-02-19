package de.tobias.playpad.project;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.page.PageSerializer;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.xml.XMLHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by tobias on 19.02.17.
 */
public class ProjectSerializer {


	private static final String ROOT_ELEMENT = "Project";
	private static final String PAGE_ELEMENT = "Page";
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

			// Load Pages
			XMLHandler<Page> handler = new XMLHandler<>(rootElement);
			List<Page> pages = handler.loadElements(PAGE_ELEMENT, new PageSerializer(project));
			for (Page page : pages) {
				project.pages.add(page);
			}

			// Load Settings
			Element settingsElement = rootElement.element(SETTINGS_ELEMENT);
			if (settingsElement != null)
				project.settings = ProjectSettingsSerializer.load(settingsElement);

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

	public static void save(Project project) throws IOException {
		Path projectPath = project.projectReference.getProjectPath();

		// Modules clearen und beim Speichern der pads neu setzen, damit alte Modules, die nicht gebracht werden, entfernt werden können.
		project.projectReference.getRequestedModules().clear();

		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement(ROOT_ELEMENT);

		// Save Pages
		XMLHandler<Page> handler = new XMLHandler<>(rootElement);
		handler.saveElements(PAGE_ELEMENT, project.pages, new PageSerializer(project));

		// Save Settings
		Element settingsElement = rootElement.addElement(SETTINGS_ELEMENT);
		ProjectSettingsSerializer.save(project.settings, settingsElement);

		if (Files.notExists(projectPath)) {
			Files.createDirectories(projectPath.getParent());
			Files.createFile(projectPath);
		}
		XMLHandler.save(projectPath, document);
	}
}
