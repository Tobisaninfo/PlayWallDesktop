package de.tobias.playpad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.project.ProjectSettings;
import de.tobias.utils.application.App;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.application.update.UpdateService;

public class VersionUpdater implements UpdateService {

	@Override
	public void update(App app, long oldVersion, long newVersion) {
		try {
			if (newVersion >= 33 && oldVersion < 33)
				update33(app);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void update33(App app) throws DocumentException, IOException {
		SAXReader reader = new SAXReader();

		Document projectsDocument = reader.read(Files.newInputStream(app.getPath(PathType.CONFIGURATION, "Projects.xml")));
		for (Object obj : projectsDocument.getRootElement().elements("Project")) {
			if (obj instanceof Element) {
				Element element = (Element) obj;

				UUID profile = UUID.fromString(element.attributeValue("profile"));
				UUID project = UUID.fromString(element.attributeValue("uuid"));

				updateProject(profile, project, app);
			}
		}
	}

	private void updateProject(UUID profile, UUID project, App app) throws DocumentException, IOException {
		Path profileSettings = app.getPath(PathType.CONFIGURATION, profile.toString(), "ProfileSettings.xml");
		SAXReader reader = new SAXReader();

		Document document = reader.read(Files.newInputStream(profileSettings));
		Element rootElement = document.getRootElement();

		int pages = Integer.valueOf(rootElement.element("PageCount").getStringValue());
		int rows = Integer.valueOf(rootElement.element("Rows").getStringValue());
		int columns = Integer.valueOf(rootElement.element("Columns").getStringValue());

		Path projectSettings = app.getPath(PathType.DOCUMENTS, project.toString() + ".xml");
		Document projectDocument = reader.read(Files.newInputStream(projectSettings));
		Element rootProjectElement = projectDocument.getRootElement();
		Element settingsElement = rootProjectElement.addElement("Settings");

		ProjectSettings projectSettings2 = new ProjectSettings();
		projectSettings2.setColumns(columns);
		projectSettings2.setRows(rows);
		projectSettings2.setPageCount(pages);

		projectSettings2.save(settingsElement);

		XMLWriter writer = new XMLWriter(Files.newOutputStream(projectSettings), OutputFormat.createPrettyPrint());
		writer.write(projectDocument);
		writer.close();
	}

}
