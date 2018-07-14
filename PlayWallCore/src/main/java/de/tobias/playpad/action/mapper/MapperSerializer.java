package de.tobias.playpad.action.mapper;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.utils.xml.XMLDeserializer;
import de.tobias.utils.xml.XMLSerializer;
import org.dom4j.Element;

/**
 * Laden und Speichern von Mappern (Array von Mappern)
 *
 * @author tobias
 * @since 5.0.1
 */
public class MapperSerializer implements XMLSerializer<Mapper>, XMLDeserializer<Mapper> {

	private static final String MAPPER_TYPE = "type";

	private Action action;

	public MapperSerializer(Action action) {
		this.action = action;
	}

	@Override
	public Mapper loadElement(Element element) {
		String mapperType = element.attributeValue(MAPPER_TYPE);

		try {
			MapperFactory component = PlayPadPlugin.getRegistryCollection().getMappers().getFactory(mapperType);

			Mapper mapper = component.createNewMapper();
			mapper.load(element, action);
			return mapper;
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void saveElement(Element newElement, Mapper data) {
		newElement.addAttribute(MAPPER_TYPE, data.getType());
		data.save(newElement);

	}

}
