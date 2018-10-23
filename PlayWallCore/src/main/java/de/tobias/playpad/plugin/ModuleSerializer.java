package de.tobias.playpad.plugin;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLSerializer;
import org.dom4j.Element;

public class ModuleSerializer implements XMLSerializer<Module>, XMLDeserializer<Module> {

	private static final String NAME_ATTR = "name";
	private static final String IDENTIFIER_ATTR = "id";

	@Override
	public Module loadElement(Element element) {
		String name = element.attributeValue(NAME_ATTR);
		String id = element.attributeValue(IDENTIFIER_ATTR);
		return new Module(name, id);
	}

	@Override
	public void saveElement(Element newElement, Module data) {
		newElement.addAttribute(NAME_ATTR, data.name);
		newElement.addAttribute(IDENTIFIER_ATTR, data.identifier);
	}
}
