package de.tobias.playpad.update;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.update.UpdateService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.UUID;

public class VersionUpdater implements UpdateService {
	@Override
	public void update(App app, long oldVersion, long newVersion) {
		try {
			if (oldVersion < 44) {
				updateTo44(app);
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void updateTo44(App app) throws DocumentException, IOException {
		Logger.debug("Updating to app version 44...");

		SAXReader reader = new SAXReader();

		Document projectsDocument = reader.read(Files.newInputStream(app.getPath(PathType.CONFIGURATION, "Projects.xml")));
		for (Element element : projectsDocument.getRootElement().elements("Project")) {
			final String projectName = element.attributeValue("name");
			final UUID projectUUID = UUID.fromString(element.attributeValue("uuid"));

			Logger.debug(MessageFormat.format("Updating project \"{0}\" (id: {1})...", projectName, projectUUID));
			Path projectPath = app.getPath(PathType.DOCUMENTS, projectUUID.toString() + ".xml");
			final Document migratedProject = updateProject(Files.newInputStream(projectPath));

			XMLWriter writer = new XMLWriter(Files.newOutputStream(projectPath), OutputFormat.createPrettyPrint());
			writer.write(migratedProject);
			writer.close();
		}
	}

	public static Document updateProject(InputStream projectInputStream) throws DocumentException {
		SAXReader reader = new SAXReader();

		Document projectDocument = reader.read(projectInputStream);
		Element rootProjectElement = projectDocument.getRootElement();

		for (Element page : rootProjectElement.elements()) {
			for (Element pad : page.elements()) {
				final Element settings = pad.element("Settings");
				if (settings == null) {
					continue;
				}

				final Element designElement = settings.element("Design");
				if (designElement == null) {
					continue;
				}

				final Attribute customFlag = designElement.attribute("custom");
				if (customFlag == null) {
					continue;
				}

				designElement.remove(customFlag);

				if (customFlag.getValue().equals("false")) {
					continue;
				}

				designElement.addElement("EnableCustomBackgroundColor").addText("true");
				designElement.addElement("EnableCustomPlayColor").addText("true");
				designElement.addElement("EnableCustomCueInColor").addText("true");
			}
		}

		return projectDocument;
	}
}
