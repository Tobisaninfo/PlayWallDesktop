package de.tobias.playpad.settings.keys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.utils.util.OS;
import de.tobias.utils.xml.XMLHandler;

/**
 * Verwaltung der Tastenkombinationen für das Menu.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class KeyCollection {

	// Schlüssel: ID, Value: Key
	private HashMap<String, KeyCollectionEntry> keys;

	/**
	 * Erstellt ein neues, leeres Mapping.
	 */
	public KeyCollection() {
		keys = new HashMap<>();
	}

	/**
	 * Fügt eine Taste zum Mapping hinzu.
	 * 
	 * @param entry
	 *            Taste
	 * @throws KeyConflictException
	 *             Registrierung fehlgeschlagen, weil Key bereits vorhanden.
	 */
	public void register(KeyCollectionEntry entry) throws KeyConflictException {
		if (!keys.containsKey(entry.getKey().getId())) {
			if (!keysConflict(entry.getKey())) {
				keys.put(entry.getKey().getId(), entry);
			} else {
				throw new KeyConflictException(entry.getKey());
			}
		}
	}

	/**
	 * Name des Keys.
	 * 
	 * @param id
	 *            ID der Kombination
	 * @return Localized Name
	 */
	public String getName(String id) {
		KeyCollectionEntry keyCollectionEntry = keys.get(id);
		if (keyCollectionEntry != null) {
			return keyCollectionEntry.getName();
		} else {
			return null;
		}
	}

	public Key getKey(String id) {
		KeyCollectionEntry keyCollectionEntry = keys.get(id);
		if (keyCollectionEntry != null) {
			return keyCollectionEntry.getKey();
		} else {
			return null;
		}
	}

	private void updateKey(Key key) {
		KeyCollectionEntry keyCollectionEntry = keys.get(key.getId());
		if (keyCollectionEntry != null)
			keyCollectionEntry.setKey(key);
	}

	public Collection<Key> getKeys() {
		return keys.values().stream().map(KeyCollectionEntry::getKey).collect(Collectors.toList());
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

	/**
	 * Prüft ob es einen Konflikt zu anderen Key Combinations gibt.
	 * 
	 * @param key
	 *            Test Objekt
	 * @return <code>true</code> Konflikt.
	 */
	public boolean keysConflict(Key key) {
		for (KeyCollectionEntry k : keys.values()) {
			if (k.getKey().getKeyCode().equals(key.getKeyCode()) && !key.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sucht nach den konkreten Konflikten.
	 * 
	 * @param key
	 *            Test Objekt
	 * @return Liste der Konflikte.
	 */
	public List<Key> getConflicts(Key key) {
		List<Key> conflicts = new ArrayList<>();
		for (KeyCollectionEntry k : keys.values()) {
			if (k.getKey().getKeyCode().equals(key.getKeyCode())) {
				conflicts.add(k.getKey());
			}
		}
		return conflicts;
	}

	/*
	 * Speicher & Laden
	 */

	private static final String KEY_ELEMENT = "Key";

	public void load(Path path) throws DocumentException, IOException {
		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			if (root.element(GlobalSettings.KEYS_ELEMENT) != null) {
				XMLHandler<Key> handler = new XMLHandler<>(root.element(GlobalSettings.KEYS_ELEMENT));
				List<Key> keys = handler.loadElements(KEY_ELEMENT, new KeySerializer());
				for (Key key : keys) {
					updateKey(key);
				}
			}
		}
	}

	public void save(Element element) {
		XMLHandler<Key> handler = new XMLHandler<>(element);
		handler.saveElements(KEY_ELEMENT, getKeys(), new KeySerializer());
	}

	private static final String WINDOWS_KEYS = "Windows";
	private static final String MAC_KEYS = "Mac";

	/**
	 * Lädt die Default Liste an vorhanden Keys.
	 * 
	 * @param classPath
	 *            Pfad zu der XML Datei mit den Keys.
	 * @param bundle
	 *            ResourceBundle für die Namen der Kombinationen.
	 */
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
						KeyCollectionEntry entry = new KeyCollectionEntry(name, key);

						try {
							register(entry);
						} catch (KeyConflictException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Lädt den Namen des Keys auf dem Bundle anhand der XML Eintrages.
	 * 
	 * @param element
	 *            XML Eintrag
	 * @param bundle
	 *            ResourceBundle
	 * @return Name oder null
	 */
	private String loadName(Element element, ResourceBundle bundle) {
		String name = element.attributeValue("name");
		if (name != null) {
			return bundle.getString(name);
		}
		return null;
	}

	/**
	 * Change an Interal Key with this new settings
	 * 
	 * @param newKey
	 *            virtal copy.
	 */
	public void editKey(Key newKey) {
		Key savedKey = getKey(newKey.getId());

		savedKey.setAlt(newKey.isAlt());
		savedKey.setCtrl(newKey.isCtrl());
		savedKey.setMeta(newKey.isMeta());
		savedKey.setShift(newKey.isShift());
		savedKey.setKey(newKey.getKey());
	}
}
