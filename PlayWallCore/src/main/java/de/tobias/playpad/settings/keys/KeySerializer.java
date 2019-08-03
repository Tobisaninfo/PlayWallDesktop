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
		boolean shift = false;
		if (element.attributeValue(SHIFT_ATTR) != null) {
			shift = Boolean.parseBoolean(element.attributeValue(SHIFT_ATTR));
		}
		boolean meta = false;
		if (element.attributeValue(META_ATTR) != null) {
			meta = Boolean.parseBoolean(element.attributeValue(META_ATTR));
		}
		boolean ctrl = false;
		if (element.attributeValue(CTRL_ATTR) != null) {
			ctrl = Boolean.parseBoolean(element.attributeValue(CTRL_ATTR));
		}
		boolean alt = false;
		if (element.attributeValue(ALT_ATTR) != null) {
			alt = Boolean.parseBoolean(element.attributeValue(ALT_ATTR));
		}

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
