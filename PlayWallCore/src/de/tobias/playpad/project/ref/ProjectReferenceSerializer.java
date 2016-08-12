package de.tobias.playpad.project.ref;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.dom4j.Element;

import de.tobias.playpad.settings.ProfileReference;
import de.tobias.playpad.xml.XMLDeserializer;
import de.tobias.playpad.xml.XMLSerializer;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class ProjectReferenceSerializer implements XMLDeserializer<ProjectReference>, XMLSerializer<ProjectReference> {

	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";
	private static final String PROFILE_ATTR = "profile";

	@Override
	public ProjectReference loadElement(Element element) {
		UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
		String name = element.attributeValue(NAME_ATTR);
		UUID profile = UUID.fromString(element.attributeValue(PROFILE_ATTR));

		ProfileReference profileRef = ProfileReference.getReference(profile);
		ProjectReference ref = new ProjectReference(uuid, name, profileRef);

		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		if (Files.exists(projectPath)) {
			try {
				ref.setLastMofied(Files.getLastModifiedTime(projectPath).toMillis());
				ref.setSize(Files.size(projectPath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ref;
	}

	@Override
	public void saveElement(Element newElement, ProjectReference data) {
		newElement.addAttribute(UUID_ATTR, data.getUuid().toString());
		newElement.addAttribute(NAME_ATTR, data.getName());
		newElement.addAttribute(PROFILE_ATTR, data.getProfileReference().getUuid().toString());
	}
}
