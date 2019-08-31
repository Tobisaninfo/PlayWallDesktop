package de.tobias.playpad.settings.keys;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLSerializer;
import org.dom4j.Element;

public class KeySerializer implements XMLSerializer<Key>, XMLDeserializer<Key> {

	private static final String ID_ATTR = "id";
	private static final String KEY_ATTR = "key";
	private static final String ALT_ATTR = "alt";
	private static final String CTRL_ATTR = "ctrl";
	private static final String META_ATTR = "meta";
	private static final String SHIFT_ATTR = "shift";

	@Override
	public Key loadElement(Element element) {
		boolean shift = loadModifier(SHIFT_ATTR, element);
		boolean meta = loadModifier(META_ATTR, element);
		boolean ctrl = loadModifier(CTRL_ATTR, element);
		boolean alt = loadModifier(ALT_ATTR, element);

		String key = "";
		if (element.attributeValue(KEY_ATTR) != null) {
			key = element.attributeValue(KEY_ATTR);
		}

		String id = "";
		if (element.attributeValue(ID_ATTR) != null) {
			id = element.attributeValue(ID_ATTR);
		}

		return new Key(id, key, ctrl, alt, meta, shift);
	}

	private boolean loadModifier(String key, Element element) {
		if (element.attributeValue(key) != null) {
			return Boolean.parseBoolean(element.attributeValue(key));
		}
		return false;
	}

	@Override
	public void saveElement(Element newElement, Key data) {
		newElement.addAttribute(SHIFT_ATTR, String.valueOf(data.isShift()));
		newElement.addAttribute(META_ATTR, String.valueOf(data.isMeta()));
		newElement.addAttribute(CTRL_ATTR, String.valueOf(data.isCtrl()));
		newElement.addAttribute(ALT_ATTR, String.valueOf(data.isAlt()));
		newElement.addAttribute(KEY_ATTR, data.getKey());
		newElement.addAttribute(ID_ATTR, data.getId());
	}
}
