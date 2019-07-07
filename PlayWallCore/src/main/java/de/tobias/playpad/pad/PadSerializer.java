package de.tobias.playpad.pad;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLSerializer;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.pad.mediapath.MediaPool;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.project.Project;
import org.dom4j.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PadSerializer implements XMLSerializer<Pad>, XMLDeserializer<Pad> {

	public static final String UUID_ATTR = "uuid";
	private static final String INDEX_ATTR = "index";
	private static final String NAME_ATTR = "name";
	private static final String STATUS_ATTR = "status";

	public static final String SETTINGS_ELEMENT = "Settings";

	private static final String CONTENT_ELEMENT = "Content";
	private static final String CONTENT_TYPE_ATTR = "type";
	private static final String CONTENT_PATHS_ELEMENT = "Paths";
	private static final String CONTENT_PATH_ELEMENT = "Path";
	private static final String CONTENT_PATH_UUID = "id";
	private static final String CONTENT_PATH_PATH = "path";
	private static final String CONTENT_PATH_FILENAME = "filename";

	private Project project;

	public PadSerializer(Project project) {
		this.project = project;
	}

	@Override
	public Pad loadElement(Element element) {
		Pad pad = new Pad(project);

		if (element.attributeValue(UUID_ATTR) != null)
			pad.setUuid(UUID.fromString(element.attributeValue(UUID_ATTR)));
		pad.setPosition(Integer.valueOf(element.attributeValue(INDEX_ATTR)));
		pad.setName(element.attributeValue(NAME_ATTR));
		PadStatus status = PadStatus.valueOf(element.attributeValue(STATUS_ATTR));
		if (status == PadStatus.EMPTY || status == PadStatus.READY)
			pad.setStatus(status);

		// Settings
		Element settingsElement = element.element(SETTINGS_ELEMENT);
		PadSettingsSerializer padSettingsSerializer = new PadSettingsSerializer();
		PadSettings settings = padSettingsSerializer.loadElement(settingsElement, pad);
		pad.setPadSettings(settings);

		// Content
		Element contentElement = element.element(CONTENT_ELEMENT);
		if (contentElement != null) {
			String contentType = contentElement.attributeValue(CONTENT_TYPE_ATTR);
			pad.setContentType(contentType);

			Element pathsElement = contentElement.element(CONTENT_PATHS_ELEMENT);
			for (Object obj : pathsElement.elements(CONTENT_PATH_ELEMENT)) {
				if (obj instanceof Element) {
					Element pathElement = (Element) obj;
					UUID uuid = UUID.fromString(pathElement.attributeValue(CONTENT_PATH_UUID));
					Path path = null;
					String filename = null;
					if (pathElement.attributeValue(CONTENT_PATH_PATH) != null) {
						path = Paths.get(pathElement.attributeValue(CONTENT_PATH_PATH));
						filename = path.getFileName().toString();
					}

					if (pathElement.attributeValue(CONTENT_PATH_FILENAME) != null) {
						filename = pathElement.attributeValue(CONTENT_PATH_FILENAME);
					}

					MediaPath mediaPath = new MediaPath(uuid, filename, pad);

					// Convert old projects to mediapool
					if (path != null) {
						MediaPool.getInstance().create(mediaPath, path);
					}

					pad.getPaths().add(mediaPath);
				}
			}
		}

		return pad;
	}

	@Override
	public void saveElement(Element element, Pad data) {
		element.addAttribute(UUID_ATTR, data.getUuid().toString());
		element.addAttribute(INDEX_ATTR, String.valueOf(data.getPosition()));
		element.addAttribute(NAME_ATTR, data.getName());
		if (data.getStatus() == PadStatus.EMPTY || data.getStatus() == PadStatus.ERROR) {
			element.addAttribute(STATUS_ATTR, PadStatus.EMPTY.name());
		} else {
			element.addAttribute(STATUS_ATTR, PadStatus.READY.name());
		}

		// Settings
		Element settingsElement = element.addElement(SETTINGS_ELEMENT);
		PadSettings settings = data.getPadSettings();
		PadSettingsSerializer serializer = new PadSettingsSerializer();
		serializer.saveElement(settingsElement, settings);

		// Content
		PadContent content = data.getContent();
		if (content != null) {
			Element contentElement = element.addElement(CONTENT_ELEMENT);
			contentElement.addAttribute(CONTENT_TYPE_ATTR, content.getType());


			Element pathsElement = contentElement.addElement(CONTENT_PATHS_ELEMENT);
			for (MediaPath mediaPath : data.getPaths()) {
				Element pathElement = pathsElement.addElement(CONTENT_PATH_ELEMENT);
				pathElement.addAttribute(CONTENT_PATH_UUID, mediaPath.getId().toString());
				if (mediaPath.getFileName() != null) {
					pathElement.addAttribute(CONTENT_PATH_FILENAME, mediaPath.getFileName());
				}
			}

			Module module = PlayPadPlugin.getRegistries().getPadContents().getModule(content.getType());
			// Für verschiedene Pad Typen wird hier das Modul gespeichert, damit das Projekt weis, welche notwendig sien beim öffnen
			project.getProjectReference().addRequestedModule(module);
		}
	}
}
