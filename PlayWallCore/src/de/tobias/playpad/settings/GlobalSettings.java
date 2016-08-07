package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.PlayPad;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.update.UpdateChannel;

/**
 * Globale Einstellungen für das Programm. Eine Instanz von diesen Einstellungen wird in {@link PlayPad} verwaltet.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 * 
 * @see PlayPad#getGlobalSettings()
 *
 */
public class GlobalSettings {

	private Path savePath;

	private KeyCollection keyCollection = new KeyCollection();

	private boolean autoUpdate = true;
	private UpdateChannel updateChannel = UpdateChannel.STABLE;

	// Getter
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public UpdateChannel getUpdateChannel() {
		return updateChannel;
	}

	public KeyCollection getKeyCollection() {
		return keyCollection;
	}

	// Setter
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public void setUpdateChannel(UpdateChannel updateChannel) {
		this.updateChannel = updateChannel;
	}

	private static final String KEYS_ELEMENT = "Keys";
	private static final String AUTO_UPDATE_ELEMENT = "AutoUpdate";
	private static final String UPDATE_CHANNEL_ELEMENT = "UpdateChannel";

	/**
	 * Lädt eine neue Instanz der Globalen Einstellungen.
	 * 
	 * @return GlobalSettings
	 * @throws DocumentException
	 *             XML Fehler
	 * @throws IOException
	 *             Fehler bei IO
	 */
	public static GlobalSettings load(Path savePath) throws DocumentException, IOException {
		GlobalSettings settings = new GlobalSettings();
		settings.savePath = savePath;

		if (Files.exists(savePath)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(savePath));
			Element root = document.getRootElement();

			if (root.element(KEYS_ELEMENT) != null)
				settings.keyCollection.load(root.element(KEYS_ELEMENT));

			if (root.element(AUTO_UPDATE_ELEMENT) != null) {
				settings.setAutoUpdate(Boolean.valueOf(root.element(AUTO_UPDATE_ELEMENT).getStringValue()));
			}

			if (root.element(UPDATE_CHANNEL_ELEMENT) != null) {
				settings.setUpdateChannel(UpdateChannel.valueOf(root.element(UPDATE_CHANNEL_ELEMENT).getStringValue()));
			}
		}
		return settings;
	}

	/**
	 * Speichert die Globalen Einstellungen
	 * 
	 * @throws UnsupportedEncodingException
	 *             Fehler bei XML
	 * @throws IOException
	 *             Fehler bei IO
	 */
	public void save() throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Config");

		// Keys
		Element keyCollectionElement = root.addElement(KEYS_ELEMENT);
		keyCollection.save(keyCollectionElement);

		// Update
		root.addElement(AUTO_UPDATE_ELEMENT).addText(String.valueOf(autoUpdate));
		root.addElement(UPDATE_CHANNEL_ELEMENT).addText(updateChannel.name());

		XMLWriter writer = new XMLWriter(Files.newOutputStream(savePath), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
