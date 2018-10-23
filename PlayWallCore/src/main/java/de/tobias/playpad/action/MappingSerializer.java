package de.tobias.playpad.action;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLHandler;
import de.thecodelabs.storage.xml.XMLSerializer;
import de.tobias.playpad.profile.Profile;
import org.dom4j.Element;

import java.util.UUID;

public class MappingSerializer implements XMLSerializer<Mapping>, XMLDeserializer<Mapping> {

	private static final String NAME = "name";
	private static final String UUID_NAME = "uuid";
	private static final String ACTION = "Action";

	private Profile profile;

	public MappingSerializer(Profile profile) {
		this.profile = profile;
	}

	@Override
	public Mapping loadElement(Element element) {
		Mapping mapping = new Mapping(false);

		mapping.setName(element.attributeValue(NAME));

		UUID uuid;
		if (element.attributeValue(UUID_NAME) != null)
			uuid = UUID.fromString(element.attributeValue(UUID_NAME));
		else
			uuid = UUID.randomUUID();
		mapping.setUuid(uuid);

		XMLHandler<Action> handler = new XMLHandler<>(element);
		handler.loadElements(ACTION, new ActionSerializer(mapping));

		return mapping;
	}

	@Override
	public void saveElement(Element newElement, Mapping data) {
		newElement.addAttribute(NAME, data.getName());
		newElement.addAttribute(UUID_NAME, data.getUuid().toString());

		XMLHandler<Action> handler = new XMLHandler<>(newElement);
		handler.saveElements(ACTION, data.getActions(), new ActionSerializer(data, profile));
	}
}
