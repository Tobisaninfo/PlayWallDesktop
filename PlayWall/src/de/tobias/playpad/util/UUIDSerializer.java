package de.tobias.playpad.util;

import de.tobias.utils.settings.UserDefaults;
import org.dom4j.Element;

import java.util.UUID;

/**
 * Created by tobias on 19.03.17.
 */
public class UUIDSerializer implements UserDefaults.Serializer<UUID> {
	@Override
	public UUID get(Element element) {
		try {
			return UUID.fromString(element.getStringValue());
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Element set(Object o, Element element) {
		return element.addText(o.toString());
	}
}
