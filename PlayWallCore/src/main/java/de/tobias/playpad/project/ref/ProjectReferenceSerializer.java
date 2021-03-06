package de.tobias.playpad.project.ref;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLHandler;
import de.thecodelabs.storage.xml.XMLSerializer;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.ModuleSerializer;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProjectReferenceSerializer implements XMLDeserializer<ProjectReference>, XMLSerializer<ProjectReference> {

	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";
	private static final String SYNC_ATTR = "sync";
	private static final String LAST_MODIFIED_ATTR = "last_modified";
	private static final String PROFILE_ATTR = "profile";
	private static final String MODULE_ELEMENT = "Module";

	@Override
	public ProjectReference loadElement(Element element) {
		UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
		String name = element.attributeValue(NAME_ATTR);

		UUID profile = null;
		if (element.attributeValue(PROFILE_ATTR) != null) {
			profile = UUID.fromString(element.attributeValue(PROFILE_ATTR));
		}
		boolean sync = Boolean.parseBoolean(element.attributeValue(SYNC_ATTR));
		long lastModified = Long.parseLong(element.attributeValue(LAST_MODIFIED_ATTR));

		XMLHandler<Module> handler = new XMLHandler<>(element);
		Set<Module> modules = new HashSet<>(handler.loadElements(MODULE_ELEMENT, new ModuleSerializer()));

		ProfileReference profileRef = ProfileReferenceManager.getReference(profile);
		ProjectReference ref = new ProjectReference(uuid, name, lastModified, profileRef, modules, sync);

		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		if (Files.exists(projectPath)) {
			try {
				ref.setLastModified(Files.getLastModifiedTime(projectPath).toMillis());
			} catch (IOException e) {
				Logger.error(e);
			}
		}
		return ref;
	}

	@Override
	public void saveElement(Element newElement, ProjectReference data) {
		newElement.addAttribute(UUID_ATTR, data.getUuid().toString());
		newElement.addAttribute(NAME_ATTR, data.getName());
		newElement.addAttribute(SYNC_ATTR, String.valueOf(data.isSync()));
		newElement.addAttribute(LAST_MODIFIED_ATTR, String.valueOf(data.getLastModified()));
		if (data.getProfileReference() != null) {
			newElement.addAttribute(PROFILE_ATTR, data.getProfileReference().getUuid().toString());
		}

		XMLHandler<Module> handler = new XMLHandler<>(newElement);
		handler.saveElements(MODULE_ELEMENT, data.getRequestedModules(), new ModuleSerializer());
	}
}
