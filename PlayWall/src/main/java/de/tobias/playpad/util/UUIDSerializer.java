package de.tobias.playpad.util;

import de.thecodelabs.storage.settings.UserDefaults;
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
	public void set(Object o, Element element) {
		element.addText(o.toString());
	}
}
