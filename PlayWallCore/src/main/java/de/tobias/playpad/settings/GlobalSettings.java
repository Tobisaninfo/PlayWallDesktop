package de.tobias.playpad.settings;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPad;
import de.tobias.playpad.settings.keys.KeyCollection;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static de.thecodelabs.versionizer.service.UpdateService.RepositoryType;

/**
 * Globale Einstellungen für das Programm. Eine Instanz von diesen Einstellungen wird in {@link PlayPad} verwaltet.
 *
 * @author tobias
 * @see PlayPad#getGlobalSettings()
 * @since 5.1.0
 */
public class GlobalSettings {

	private Path savePath;

	// Key Binding
	private KeyCollection keyCollection = new KeyCollection();

	// Update
	private boolean autoUpdate = true;
	private boolean ignoreUpdate = false;
	private RepositoryType updateChannel = RepositoryType.RELEASE;

	// Live Mode
	private boolean liveMode = false;
	private boolean liveModePage = false;
	private boolean liveModeDrag = false;
	private boolean liveModeFile = false;
	private boolean liveModeSettings = false;

	// Paths
	private Path cachePath = ApplicationUtils.getApplication().getPath(PathType.CACHE);

	// Dialogs
	private boolean ignoreSaveDialog = false;

	// Behaviour
	private boolean openLastDocument = false;

	public GlobalSettings() {
	}

	// Getter
	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public RepositoryType getUpdateChannel() {
		return updateChannel;
	}

	public boolean isIgnoreUpdate() {
		return ignoreUpdate;
	}

	public KeyCollection getKeyCollection() {
		return keyCollection;
	}

	public boolean isLiveMode() {
		return liveMode;
	}

	public boolean isLiveModeDrag() {
		return liveModeDrag;
	}

	public boolean isLiveModeFile() {
		return liveModeFile;
	}

	public boolean isLiveModePage() {
		return liveModePage;
	}

	public boolean isLiveModeSettings() {
		return liveModeSettings;
	}

	public Path getCachePath() {
		return cachePath;
	}

	public boolean isIgnoreSaveDialog() {
		return ignoreSaveDialog;
	}

	public boolean isOpenLastDocument() {
		return openLastDocument;
	}

	// Setter
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	public void setUpdateChannel(RepositoryType updateChannel) {
		this.updateChannel = updateChannel;
	}

	public void setIgnoreUpdate(boolean ignoreUpdate) {
		this.ignoreUpdate = ignoreUpdate;
	}

	public void setLiveMode(boolean liveMode) {
		this.liveMode = liveMode;
	}

	public void setLiveModeDrag(boolean liveModeDrag) {
		this.liveModeDrag = liveModeDrag;
	}

	public void setLiveModeFile(boolean liveModeFile) {
		this.liveModeFile = liveModeFile;
	}

	public void setLiveModePage(boolean liveModePage) {
		this.liveModePage = liveModePage;
	}

	public void setLiveModeSettings(boolean liveModeSettings) {
		this.liveModeSettings = liveModeSettings;
	}

	public void setCachePath(Path cachePath) {
		this.cachePath = cachePath;
	}

	public void setIgnoreSaveDialog(boolean ignoreSaveDialog) {
		this.ignoreSaveDialog = ignoreSaveDialog;
	}

	public void setOpenLastDocument(boolean openLastDocument) {
		this.openLastDocument = openLastDocument;
	}

	// Save & Load Data

	public static final String KEYS_ELEMENT = "Keys";
	private static final String AUTO_UPDATE_ELEMENT = "AutoUpdate";
	private static final String IGNORE_UPDATE_ELEMENT = "IgnoreUpdate";
	private static final String UPDATE_CHANNEL_ELEMENT = "UpdateChannel";
	private static final String LIVE_MODE_ELEMENT = "LiveMode";
	private static final String LIVE_MODE_PAGE_ATTR = "page";
	private static final String LIVE_MODE_DRAG_ATTR = "drag";
	private static final String LIVE_MODE_FILE_ATTR = "file";
	private static final String LIVE_MODE_SETTINGS_ATTR = "settings";
	private static final String CACHE_PATH_ELEMENT = "Cache-Path";
	private static final String IGNORE_SAVE_DIALOG_ELEMENT = "IgnoreSaveDialog";
	private static final String OPEN_LAST_DOCUMENT_ELEMENT = "OpenLastDocument";

	/**
	 * Lädt eine neue Instanz der Globalen Einstellungen.
	 *
	 * @param savePath path to be saved
	 * @return GlobalSettings
	 */
	public static GlobalSettings load(Path savePath) {
		try {
			GlobalSettings settings = new GlobalSettings();
			settings.savePath = savePath;

			if (Files.exists(savePath)) {
				SAXReader reader = new SAXReader();
				Document document = reader.read(Files.newInputStream(savePath));
				Element root = document.getRootElement();

				if (root.element(AUTO_UPDATE_ELEMENT) != null) {
					settings.setAutoUpdate(Boolean.valueOf(root.element(AUTO_UPDATE_ELEMENT).getStringValue()));
				}

				if (root.element(IGNORE_UPDATE_ELEMENT) != null) {
					settings.setIgnoreUpdate(Boolean.valueOf(root.element(IGNORE_UPDATE_ELEMENT).getStringValue()));
				}

				if (root.element(UPDATE_CHANNEL_ELEMENT) != null) {
					try {
						final RepositoryType repositoryType = RepositoryType.valueOf(root.element(UPDATE_CHANNEL_ELEMENT).getStringValue());
						settings.setUpdateChannel(repositoryType);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}

				Element liveElement = root.element(LIVE_MODE_ELEMENT);
				if (liveElement != null) {
					settings.setLiveMode(Boolean.valueOf(liveElement.getStringValue()));
					if (liveElement.attributeValue(LIVE_MODE_PAGE_ATTR) != null) {
						settings.setLiveModePage(Boolean.valueOf(liveElement.attributeValue(LIVE_MODE_PAGE_ATTR)));
					}
					if (liveElement.attributeValue(LIVE_MODE_DRAG_ATTR) != null) {
						settings.setLiveModeDrag(Boolean.valueOf(liveElement.attributeValue(LIVE_MODE_DRAG_ATTR)));
					}
					if (liveElement.attributeValue(LIVE_MODE_FILE_ATTR) != null) {
						settings.setLiveModeFile(Boolean.valueOf(liveElement.attributeValue(LIVE_MODE_FILE_ATTR)));
					}
					if (liveElement.attributeValue(LIVE_MODE_SETTINGS_ATTR) != null) {
						settings.setLiveModeSettings(Boolean.valueOf(liveElement.attributeValue(LIVE_MODE_SETTINGS_ATTR)));
					}
				}

				if (root.element(CACHE_PATH_ELEMENT) != null) {
					settings.setCachePath(Paths.get(root.element(CACHE_PATH_ELEMENT).getStringValue()));
				}

				// Dialogs
				if (root.element(IGNORE_SAVE_DIALOG_ELEMENT) != null) {
					settings.setIgnoreSaveDialog(Boolean.valueOf(root.element(IGNORE_SAVE_DIALOG_ELEMENT).getStringValue()));
				}

				// Behaviour
				if (root.element(OPEN_LAST_DOCUMENT_ELEMENT) != null) {
					settings.setOpenLastDocument(Boolean.valueOf(root.element(OPEN_LAST_DOCUMENT_ELEMENT).getStringValue()));
				}
			}
			return settings;
		} catch (DocumentException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Save the global settings into a file.
	 */
	public void save() {
		try {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement("Config");

			// Keys
			Element keyCollectionElement = root.addElement(KEYS_ELEMENT);
			keyCollection.save(keyCollectionElement);

			// Update
			root.addElement(AUTO_UPDATE_ELEMENT).addText(String.valueOf(autoUpdate));
			root.addElement(IGNORE_UPDATE_ELEMENT).addText(String.valueOf(ignoreUpdate));
			root.addElement(UPDATE_CHANNEL_ELEMENT).addText(updateChannel.name());

			// Live Mode
			Element liveElement = root.addElement(LIVE_MODE_ELEMENT);
			liveElement.addText(String.valueOf(liveMode));
			liveElement.addAttribute(LIVE_MODE_PAGE_ATTR, String.valueOf(liveModePage));
			liveElement.addAttribute(LIVE_MODE_DRAG_ATTR, String.valueOf(liveModeDrag));
			liveElement.addAttribute(LIVE_MODE_FILE_ATTR, String.valueOf(liveModeFile));
			liveElement.addAttribute(LIVE_MODE_SETTINGS_ATTR, String.valueOf(liveModeSettings));

			// Paths
			root.addElement(CACHE_PATH_ELEMENT).addText(cachePath.toString());

			// Dialogs
			root.addElement(IGNORE_SAVE_DIALOG_ELEMENT).addText(String.valueOf(ignoreSaveDialog));

			// Behaviour
			root.addElement(OPEN_LAST_DOCUMENT_ELEMENT).addText(String.valueOf(openLastDocument));

			XMLWriter writer = new XMLWriter(Files.newOutputStream(savePath), OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
