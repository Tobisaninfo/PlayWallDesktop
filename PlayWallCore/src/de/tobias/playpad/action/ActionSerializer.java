package de.tobias.playpad.action;

import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperSerializer;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.profile.Profile;
import de.tobias.utils.xml.XMLDeserializer;
import de.tobias.utils.xml.XMLHandler;
import de.tobias.utils.xml.XMLSerializer;

public class ActionSerializer implements XMLSerializer<Action>, XMLDeserializer<Action> {

	private static final String ACTION_TYPE = "type";
	private static final String MAPPER = "Mapper";

	private Mapping mapping;
	private Profile profile;

	public ActionSerializer(Mapping mapping) {
		this.mapping = mapping;
	}

	/**
	 * Speichern.
	 * 
	 * @param mapping
	 *            mapping
	 * @param profile
	 *            profile
	 */
	public ActionSerializer(Mapping mapping, Profile profile) {
		this.mapping = mapping;
		this.profile = profile;
	}

	@Override
	public Action loadElement(Element element) {
		String tpye = element.attributeValue(ACTION_TYPE);

		try {
			Action action = PlayPadPlugin.getRegistryCollection().getActions().getFactory(tpye).newInstance();
			action.load(element);

			boolean added = mapping.addActionIfNotContains(action);

			if (added) {
				XMLHandler<Mapper> handler = new XMLHandler<>(element);
				List<Mapper> mappers = handler.loadElements(MAPPER, new MapperSerializer(action));
				mappers.forEach(action::addMapper);
			}

			return action;
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void saveElement(Element newElement, Action data) {
		Module module = PlayPadPlugin.getRegistryCollection().getActions().getModule(data.getType());
		if (profile != null) {
			profile.getRef().addRequestedModule(module);
		}

		newElement.addAttribute(ACTION_TYPE, data.getType());

		data.save(newElement);

		XMLHandler<Mapper> handler = new XMLHandler<>(newElement);
		handler.saveElements(MAPPER, data.getMappers(), new MapperSerializer(data));
	}

}
