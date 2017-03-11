package de.tobias.playpad.project;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSerializer;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadSettingsSerializer;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Created by tobias on 22.02.17.
 */
public class ProjectSyncSerializer implements ProjectReader {

	@Override
	public Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProfileNotFoundException {
		Server server = PlayPadPlugin.getServerHandler().getServer();
		Project project = server.getProject(projectReference);

		// Load additional Information
		Path path = projectReference.getProjectPath();
		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element rootElement = document.getRootElement();

			// Project Settings
			Element projectSettingsElement = rootElement.element(ProjectSerializer.SETTINGS_ELEMENT);
			project.settings = ProjectSettingsSerializer.load(projectSettingsElement);

			// Pad Settings
			PadSettingsSerializer padSettingsSerializer = new PadSettingsSerializer();
			for (Object pageObj : rootElement.elements(ProjectSerializer.PAGE_ELEMENT)) {
				Element pageElement = (Element) pageObj;
				for (Object padObj : pageElement.elements(ProjectSerializer.PAD_ELEMENT)) {
					Element padElement = (Element) padObj;
					Element settingsElement = padElement.element(PadSerializer.SETTINGS_ELEMENT);

					UUID uuid = UUID.fromString(padElement.attributeValue(PadSerializer.UUID_ATTR));
					Pad pad = project.getPad(uuid);
					if (pad != null) {
						PadSettings settings = padSettingsSerializer.loadElement(settingsElement, pad);
						pad.setPadSettings(settings);
					}
				}
			}
		}
		return project;
	}
}
