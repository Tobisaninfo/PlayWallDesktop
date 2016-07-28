package de.tobias.playpad.action;

import org.dom4j.Element;

import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.xml.XMLDeserializer;
import de.tobias.playpad.xml.XMLSerializer;

public class MapperSerializer implements XMLSerializer<Mapper>, XMLDeserializer<Mapper> {

	private static final String MAPPER_TYPE = "type";

	private Action action;

	public MapperSerializer(Action action) {
		this.action = action;
	}

	@Override
	public Mapper loadElement(Element element) {
		String mapperType = element.attributeValue(MAPPER_TYPE);

		Mapper mapper = MapperRegistry.getMapperConnect(mapperType).createNewMapper();
		mapper.load(element, action);
		return mapper;
	}

	@Override
	public void saveElement(Element newElement, Mapper data) {
		newElement.addAttribute(MAPPER_TYPE, data.getType());
		data.save(newElement, action);

	}

}
