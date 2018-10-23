package de.tobias.playpad.profile.ref;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLHandler;
import de.thecodelabs.storage.xml.XMLSerializer;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.ModuleSerializer;
import org.dom4j.Element;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Util zum arbeiten mit XML und ProfileReference
 *
 * @author tobias
 * @see ProfileReference
 * @since 5.0.1
 */
public class ProfileReferenceSerializer implements XMLSerializer<ProfileReference>, XMLDeserializer<ProfileReference> {

	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";
	private static final String MODULE_ELEMENT = "Module";

	@Override
	public ProfileReference loadElement(Element element) {
		UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
		String name = element.attributeValue(NAME_ATTR);

		XMLHandler<Module> handler = new XMLHandler<>(element);
		Set<Module> modules = new HashSet<>(handler.loadElements(MODULE_ELEMENT, new ModuleSerializer()));

		return new ProfileReference(uuid, name, modules);
	}

	@Override
	public void saveElement(Element newElement, ProfileReference data) {
		newElement.addAttribute(UUID_ATTR, data.getUuid().toString());
		newElement.addAttribute(NAME_ATTR, data.getName());

		XMLHandler<Module> handler = new XMLHandler<>(newElement);
		handler.saveElements(MODULE_ELEMENT, data.getRequestedModules(), new ModuleSerializer());
	}

}
