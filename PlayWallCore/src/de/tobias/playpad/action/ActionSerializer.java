package de.tobias.playpad.action;

import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.xml.XMLDeserializer;
import de.tobias.playpad.xml.XMLHandler;
import de.tobias.playpad.xml.XMLSerializer;

public class ActionSerializer implements XMLSerializer<Action>, XMLDeserializer<Action> {

	private static final String ACTION_TYPE = "type";
	private static final String MAPPER = "Mapper";

	private Mapping mapping;

	public ActionSerializer(Mapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public Action loadElement(Element element) {
		String tpye = element.attributeValue(ACTION_TYPE);

		Action action = ActionRegistery.getActionConnect(tpye).newInstance();
		action.load(element);

		boolean added = mapping.addActionIfNotContains(action);

		if (added) {
			XMLHandler<Mapper> handler = new XMLHandler<>(element);
			List<Mapper> mappers = handler.loadElements(MAPPER, new MapperSerializer(action));
			mappers.forEach(action::addMapper);
		}

		return action;
	}

	@Override
	public void saveElement(Element newElement, Action data) {
		newElement.addAttribute(ACTION_TYPE, data.getType());

		data.save(newElement);

		XMLHandler<Mapper> handler = new XMLHandler<>(newElement);
		handler.saveElements(MAPPER, data.getMappers(), new MapperSerializer(data));
	}

}