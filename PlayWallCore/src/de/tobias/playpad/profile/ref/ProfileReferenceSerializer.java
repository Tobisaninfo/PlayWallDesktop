package de.tobias.playpad.profile.ref;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.dom4j.Element;

import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.ModuleSerializer;
import de.tobias.utils.xml.XMLDeserializer;
import de.tobias.utils.xml.XMLHandler;
import de.tobias.utils.xml.XMLSerializer;

/**
 * Util zum arbeiten mit XML und ProfileReference
 * 
 * @author tobias
 * 
 * @since 5.0.1
 * @see ProfileReference
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

		ProfileReference ref = new ProfileReference(uuid, name, modules);
		return ref;
	}

	@Override
	public void saveElement(Element newElement, ProfileReference data) {
		newElement.addAttribute(UUID_ATTR, data.getUuid().toString());
		newElement.addAttribute(NAME_ATTR, data.getName());

		XMLHandler<Module> handler = new XMLHandler<>(newElement);
		handler.saveElements(MODULE_ELEMENT, data.getRequestedModules(), new ModuleSerializer());
	}

}
