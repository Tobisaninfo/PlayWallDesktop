package de.tobias.playpad.project;

import de.thecodelabs.storage.xml.XMLHandler;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.page.PageSerializer;
import de.tobias.playpad.project.ref.ProjectReference;
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
public class ProjectSerializer implements ProjectReader, ProjectWriter {


	private static final String ROOT_ELEMENT = "Project";
	static final String PAGE_ELEMENT = "Page";
	public static final String PAD_ELEMENT = "Pad";
	static final String SETTINGS_ELEMENT = "Settings";

	@Override
	public Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProjectNotFoundException {
		Path projectPath = projectReference.getProjectPath();
		if (Files.notExists(projectPath)) {
			throw new ProjectNotFoundException(projectReference);
		}

		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(projectPath));
		Element rootElement = document.getRootElement();

		Project project = new Project(projectReference);

		// Load Pages
		XMLHandler<Page> handler = new XMLHandler<>(rootElement);
		List<Page> pages = handler.loadElements(PAGE_ELEMENT, new PageSerializer(project));
		project.pages.addAll(pages);

		// Load Settings
		Element settingsElement = rootElement.element(SETTINGS_ELEMENT);
		if (settingsElement != null)
			project.settings = ProjectSettingsSerializer.load(settingsElement);
		return project;
	}

	@Override
	public void write(Project project) throws IOException {
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
