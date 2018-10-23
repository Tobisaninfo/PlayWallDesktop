package de.tobias.playpad.project.importer;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tobias on 25.02.17.
 */
public class ConverterV6 {

	private static final String CONTENT_TYPE_ATTR = "type";
	private static final String CONTENT_PATHS_ELEMENT = "Paths";
	private static final String CONTENT_PATH_ELEMENT = "Path";
	private static final String CONTENT_PATH_UUID = "id";
	private static final String CONTENT_PATH_PATH = "path";

	public static void convert(UUID src, String name) throws IOException, DocumentException {
		ProjectReference ref = new ProjectReference(src, name);

		Path srcPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS).resolve("../../de.tobias.playpad.v6/Documents/" + src + ".xml");
		Path desPath = ref.getProjectPath();
		Files.copy(srcPath, desPath, StandardCopyOption.REPLACE_EXISTING);

		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(desPath));
		Element rootElement = document.getRootElement();
		for (Object pageObj : rootElement.elements("Page")) {
			Element pageElement = (Element) pageObj;
			for (Object padObj : pageElement.elements("Pad")) {
				Element padElement = (Element) padObj;
				Element contentElement = padElement.element("Content");
				if (contentElement != null) {
					String type = contentElement.attributeValue("type");
					String path = contentElement.getStringValue();

					// Remove Old
					padElement.remove(contentElement);

					// Add New
					contentElement = padElement.addElement("Content");
					contentElement.addAttribute(CONTENT_TYPE_ATTR, type);

					Element pathsElement = contentElement.addElement(CONTENT_PATHS_ELEMENT);

					Element pathElement = pathsElement.addElement(CONTENT_PATH_ELEMENT);
					pathElement.addAttribute(CONTENT_PATH_UUID, UUID.randomUUID().toString());
					pathElement.addAttribute(CONTENT_PATH_PATH, path);
				}
			}
		}
		XMLWriter writer = new XMLWriter(Files.newOutputStream(desPath), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static List<ProjectReference> loadProjectReferences() throws IOException, DocumentException {
		List<ProjectReference> projects = new ArrayList<>();
		Path projectReferencesPath = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION).resolve("../../de.tobias.playpad.v6/Config/Projects.xml");
		if (Files.exists(projectReferencesPath)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(projectReferencesPath));
			for (Object object : document.getRootElement().elements(ProjectReferenceManager.PROJECT_ELEMENT)) {
				if (object instanceof Element) {
					Element element = (Element) object;

					UUID uuid = UUID.fromString(element.attributeValue("uuid"));
					String name = element.attributeValue("name");

					ProjectReference ref = new ProjectReference(uuid, name);
					projects.add(ref);
				}
			}
		}
		return projects;
	}

}
