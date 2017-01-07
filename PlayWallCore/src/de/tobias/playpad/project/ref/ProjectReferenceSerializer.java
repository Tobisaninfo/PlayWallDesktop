package de.tobias.playpad.project.ref;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dom4j.Element;

import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.ModuleSerializer;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferences;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.xml.XMLDeserializer;
import de.tobias.utils.xml.XMLHandler;
import de.tobias.utils.xml.XMLSerializer;

public class ProjectReferenceSerializer implements XMLDeserializer<ProjectReference>, XMLSerializer<ProjectReference> {

	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";
	private static final String PROFILE_ATTR = "profile";
	private static final String MODULE_ELEMENT = "Module";

	@Override
	public ProjectReference loadElement(Element element) {
		UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
		String name = element.attributeValue(NAME_ATTR);
		UUID profile = UUID.fromString(element.attributeValue(PROFILE_ATTR));

		XMLHandler<Module> handler = new XMLHandler<>(element);
		Set<Module> modules = new HashSet<>(handler.loadElements(MODULE_ELEMENT, new ModuleSerializer()));

		ProfileReference profileRef = ProfileReferences.getReference(profile);
		ProjectReference ref = new ProjectReference(uuid, name, profileRef, modules);

		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		if (Files.exists(projectPath)) {
			try {
				ref.setLastModified(Files.getLastModifiedTime(projectPath).toMillis());
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

		XMLHandler<Module> handler = new XMLHandler<>(newElement);
		handler.saveElements(MODULE_ELEMENT, data.getRequestedModules(), new ModuleSerializer());
	}
}
