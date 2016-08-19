package de.tobias.playpad.settings;

import java.util.UUID;

import org.dom4j.Element;

import de.tobias.utils.xml.XMLDeserializer;
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

	@Override
	public ProfileReference loadElement(Element element) {
		UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
		String name = element.attributeValue(NAME_ATTR);

		ProfileReference ref = new ProfileReference(uuid, name);
		return ref;
	}

	@Override
	public void saveElement(Element newElement, ProfileReference data) {
		newElement.addAttribute(UUID_ATTR, data.getUuid().toString());
		newElement.addAttribute(NAME_ATTR, data.getName());
	}

}
