package de.tobias.playpad.settings.keys;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.xml.XMLHandler;
import de.tobias.utils.util.OS;

/**
 * Verwaltung der Tastenkombinationen für das Menu.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class KeyCollection {

	// Schlüssel: ID, Value: Key
	private HashMap<String, Key> keys;
	private HashMap<String, String> names;

	public KeyCollection() {
		keys = new HashMap<>();
		names = new HashMap<>();
	}

	public void register(Key key) {
		if (!keys.containsKey(key.getId())) {
			if (!keysConflict(key)) {
				keys.put(key.getId(), key);
			}
		}
	}

	public String getName(String id) {
		return names.get(id);
	}

	public Key getKey(String id) {
		return keys.get(id);
	}

	public Collection<Key> getKeys() {
		return keys.values();
	}

	/**
	 * Löscht eine Tastenkombination.
	 * 
	 * @param key
	 *            Key
	 */
	public void removeKeyBinding(Key key) {
		key.setAlt(false);
		key.setCtrl(false);
		key.setMeta(false);
		key.setShift(false);
		key.setKey("");
	}

	public boolean keysConflict(Key key) {
		for (Key k : keys.values()) {
			if (k.getKeyCode().equals(key.getKeyCode())) {
				return true;
			}
		}
		return false;
	}

	private static final String KEY_ELEMENT = "Key";

	public void load(Element element) {
		XMLHandler<Key> handler = new XMLHandler<>(element);
		List<Key> keys = handler.loadElements(KEY_ELEMENT, new KeySerializer());
		for (Key key : keys) {
			register(key);
		}
	}

	public void save(Element element) {
		XMLHandler<Key> handler = new XMLHandler<>(element);
		handler.saveElements(KEY_ELEMENT, getKeys(), new KeySerializer());
	}

	private static final String WINDOWS_KEYS = "Windows";
	private static final String MAC_KEYS = "Mac";

	public void loadDefaultFromFile(String classPath, ResourceBundle bundle) {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(getClass().getClassLoader().getResourceAsStream(classPath));
			Element rootElement = document.getRootElement();

			Element keysElement = null;
			if (OS.isWindows())
				keysElement = rootElement.element(WINDOWS_KEYS);
			else if (OS.isMacOS())
				keysElement = rootElement.element(MAC_KEYS);

			if (keysElement != null) {
				KeySerializer keySerializer = new KeySerializer();

				for (Object obj : keysElement.elements(KEY_ELEMENT)) {
					if (obj instanceof Element) {
						Element keyElement = (Element) obj;

						String name = loadName(keyElement, bundle);
						Key key = keySerializer.loadElement(keyElement);

						names.put(key.getId(), name);
						register(key);
					}
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private String loadName(Element element, ResourceBundle bundle) {
		String name = element.attributeValue("name");
		if (name != null) {
			return bundle.getString(name);
		}
		return null;
	}

	public void editKey(Key newKey) {
		Key savedKey = getKey(newKey.getId());

		savedKey.setAlt(newKey.isAlt());
		savedKey.setCtrl(newKey.isCtrl());
		savedKey.setMeta(newKey.isMeta());
		savedKey.setShift(newKey.isShift());
		savedKey.setKey(newKey.getKey());
	}
}
