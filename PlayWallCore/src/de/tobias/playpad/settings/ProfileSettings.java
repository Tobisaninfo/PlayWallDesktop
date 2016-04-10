package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.playpad.pad.Fade;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.Warning;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.settings.UserDefaults;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

public class ProfileSettings {

	public static final int MAX_PAGES = 8;

	// MIDI
	private String midiDevice;

	// GridPane
	private int pageCount = 2;
	private int columns = 6;
	private int rows = 5;

	// Audio Output
	private String audioClass = AudioRegistry.getDefaultAudioInterface();
	private HashMap<String, Object> audioUserInfo = new HashMap<>();

	// Layout
	private String layoutType = LayoutRegistry.getDefaultLayout();

	// Cart Settings
	private Warning warningFeedback = new Warning(Duration.seconds(5));

	private boolean midiActive = false;
	private boolean liveMode = true;
	private DoubleProperty volume = new SimpleDoubleProperty(1.0);

	private boolean windowAlwaysOnTop = false;

	private Fade fade = new Fade();
	private TimeMode player_timeDisplayMode = TimeMode.REST;

	private boolean dialogDragAndDrop = true;

	private Path cachePath = ApplicationUtils.getApplication().getPath(PathType.CACHE);

	private boolean autoUpdate = true;

	// Getter
	public String getMidiDevice() {
		return midiDevice;
	}

	public int getPageCount() {
		return pageCount;
	}

	/**
	 * Returns the value of colums (Number of cells form left to right)
	 * 
	 * @return columns
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * Returns the value of rows (Number of cells from top to bottom
	 * 
	 * @return rows
	 */
	public int getRows() {
		return rows;
	}

	public Path getCachePath() {
		return cachePath;
	}

	public String getLayoutType() {
		return layoutType;
	}

	public Warning getWarningFeedback() {
		return warningFeedback;
	}

	public boolean isMidiActive() {
		return midiActive;
	}

	public boolean isLiveMode() {
		return liveMode;
	}

	public double getVolume() {
		return volume.get();
	}

	public boolean isWindowAlwaysOnTop() {
		return windowAlwaysOnTop;
	}

	public Fade getFade() {
		return fade;
	}

	public TimeMode getPlayerTimeDisplayMode() {
		return player_timeDisplayMode;
	}

	public boolean isDialogDragAndDrop() {
		return dialogDragAndDrop;
	}

	public String getAudioClass() {
		return audioClass;
	}

	public HashMap<String, Object> getAudioUserInfo() {
		return audioUserInfo;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	// Setter
	public void setMidiDeviceName(String midiDevice) {
		this.midiDevice = midiDevice;
	}

	public void setPageCount(int pageCount) {
		if (pageCount > MAX_PAGES)
			pageCount = MAX_PAGES;
		this.pageCount = pageCount;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setCachePath(Path cachePath) {
		this.cachePath = cachePath;
	}

	public void setLayoutType(String layoutType) {
		this.layoutType = layoutType;
	}

	public void setWarningFeedback(Warning warningFeedback) {
		this.warningFeedback = warningFeedback;
	}

	public void setMidiActive(boolean midiActive) {
		this.midiActive = midiActive;
	}

	public void setLiveMode(boolean liveMode) {
		this.liveMode = liveMode;
	}

	public void setVolume(double volume) {
		this.volume.set(volume);
	}

	public void setWindowAlwaysOnTop(boolean windowAlwaysOnTop) {
		this.windowAlwaysOnTop = windowAlwaysOnTop;
	}

	public void setFade(Fade fade) {
		this.fade = fade;
	}

	public void setPlayerTimeDisplayMode(TimeMode player_timeDisplayMode) {
		this.player_timeDisplayMode = player_timeDisplayMode;
	}

	public void setDialogDragAndDrop(boolean dialogDragAndDrop) {
		this.dialogDragAndDrop = dialogDragAndDrop;
	}

	public void setAudioClass(String audioClass) {
		this.audioClass = audioClass;
	}

	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	// Properties
	public DoubleProperty volumeProperty() {
		return volume;
	}

	private static final String ITEM_ELEMENT = "Item";
	private static final String AUTO_UPDATE_ELEMENT = "AutoUpdate";
	private static final String CACHE_PATH_ELEMENT = "Cache-Path";
	private static final String VOLUME_ELEMENT = "Volume";
	private static final String KEY_ATTRIBUTE = "key";
	private static final String AUDIO_USER_INFO_ELEMENT = "AudioUserInfo";
	private static final String AUDIO_CLASS_ELEMENT = "AudioClass";
	private static final String DRAG_AND_DROP_DIALOG_ELEMENT = "DragAndDropDialog";
	private static final String WINDOW_ALWAYS_ON_TOP_ELEMENT = "WindowAlwaysOnTop";
	private static final String LIVE_MODE_ELEMENT = "LiveMode";
	private static final String TIME_DISPLAY_ELEMENT = "TimeDisplay";
	private static final String FADE_ELEMENT = "Fade";
	private static final String WARNING_ELEMENT = "Warning";
	private static final String LAYOUT_TYPE_ELEMENT = "LayoutType";
	private static final String ROWS_ELEMENT = "Rows";
	private static final String COLUMNS_ELEMENT = "Columns";
	private static final String PAGE_COUNT_ELEMENT = "PageCount";
	private static final String MIDI_ACTIVE_ELEMENT = "MidiActive";
	private static final String MIDI_DEVICE_ELEMENT = "MidiDevice";

	// File Handler
	public static ProfileSettings load(Path path) throws DocumentException, IOException {
		ProfileSettings profileSettings = new ProfileSettings();

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			if (root.element(MIDI_DEVICE_ELEMENT) != null)
				profileSettings.setMidiDeviceName(root.element(MIDI_DEVICE_ELEMENT).getStringValue());
			if (root.element(MIDI_ACTIVE_ELEMENT) != null)
				profileSettings.setMidiActive(Boolean.valueOf(root.element(MIDI_ACTIVE_ELEMENT).getStringValue()));

			if (root.element(PAGE_COUNT_ELEMENT) != null)
				profileSettings.setPageCount(Integer.valueOf(root.element(PAGE_COUNT_ELEMENT).getStringValue()));
			if (root.element(COLUMNS_ELEMENT) != null)
				profileSettings.setColumns(Integer.valueOf(root.element(COLUMNS_ELEMENT).getStringValue()));
			if (root.element(ROWS_ELEMENT) != null)
				profileSettings.setRows(Integer.valueOf(root.element(ROWS_ELEMENT).getStringValue()));

			if (root.element(LAYOUT_TYPE_ELEMENT) != null) {
				profileSettings.setLayoutType(root.element(LAYOUT_TYPE_ELEMENT).getStringValue());
			}

			if (root.element(WARNING_ELEMENT) != null) {
				Warning warning = Warning.loadV2(root.element(WARNING_ELEMENT));
				if (warning != null) {
					profileSettings.setWarningFeedback(warning);
				}
			}

			if (root.element(FADE_ELEMENT) != null) {
				Fade fade = Fade.load(root.element(FADE_ELEMENT));
				profileSettings.setFade(fade);
			}

			if (root.element(TIME_DISPLAY_ELEMENT) != null) {
				try {
					TimeMode timeMode = TimeMode.valueOf(root.element(TIME_DISPLAY_ELEMENT).getStringValue());
					profileSettings.setPlayerTimeDisplayMode(timeMode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (root.element(LIVE_MODE_ELEMENT) != null)
				profileSettings.setLiveMode(Boolean.valueOf(root.element(LIVE_MODE_ELEMENT).getStringValue()));
			if (root.element(WINDOW_ALWAYS_ON_TOP_ELEMENT) != null)
				profileSettings.setWindowAlwaysOnTop(Boolean.valueOf(root.element(WINDOW_ALWAYS_ON_TOP_ELEMENT).getStringValue()));
			if (root.element(DRAG_AND_DROP_DIALOG_ELEMENT) != null)
				profileSettings.setDialogDragAndDrop(Boolean.valueOf(root.element(DRAG_AND_DROP_DIALOG_ELEMENT).getStringValue()));
			if (root.element(AUDIO_CLASS_ELEMENT) != null)
				profileSettings.setAudioClass(root.element(AUDIO_CLASS_ELEMENT).getStringValue());

			Element userInfoElement = root.element(AUDIO_USER_INFO_ELEMENT);
			if (userInfoElement != null) {
				for (Object object : userInfoElement.elements()) {
					if (object instanceof Element) {
						Element item = (Element) object;
						String key = item.attributeValue(KEY_ATTRIBUTE);
						Object data = UserDefaults.loadElement(item);
						profileSettings.audioUserInfo.put(key, data);
					}
				}
			}
			if (root.element(VOLUME_ELEMENT) != null)
				profileSettings.setVolume(Double.valueOf(root.element(VOLUME_ELEMENT).getStringValue()));

			if (root.element(CACHE_PATH_ELEMENT) != null) {
				profileSettings.setCachePath(Paths.get(root.element(CACHE_PATH_ELEMENT).getStringValue()));
			}

			if (root.element(AUTO_UPDATE_ELEMENT) != null) {
				profileSettings.setAutoUpdate(Boolean.valueOf(root.element(AUTO_UPDATE_ELEMENT).getStringValue()));
			}
		}
		return profileSettings;
	}

	public void save(Path path) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Config");

		// MIDI
		if (midiDevice != null)
			root.addElement(MIDI_DEVICE_ELEMENT).addText(midiDevice);
		root.addElement(MIDI_ACTIVE_ELEMENT).addText(String.valueOf(midiActive));

		// GirdPane
		root.addElement(PAGE_COUNT_ELEMENT).addText(String.valueOf(pageCount));
		root.addElement(COLUMNS_ELEMENT).addText(String.valueOf(columns));
		root.addElement(ROWS_ELEMENT).addText(String.valueOf(rows));

		root.addElement(LAYOUT_TYPE_ELEMENT).addText(layoutType);

		warningFeedback.save(root.addElement(WARNING_ELEMENT));
		fade.save(root.addElement(FADE_ELEMENT));
		root.addElement(TIME_DISPLAY_ELEMENT).addText(player_timeDisplayMode.name());

		root.addElement(LIVE_MODE_ELEMENT).addText(String.valueOf(liveMode));
		root.addElement(WINDOW_ALWAYS_ON_TOP_ELEMENT).addText(String.valueOf(windowAlwaysOnTop));
		root.addElement(DRAG_AND_DROP_DIALOG_ELEMENT).addText(String.valueOf(dialogDragAndDrop));

		// Audio
		root.addElement(AUDIO_CLASS_ELEMENT).addText(audioClass);
		Element userInfoElement = root.addElement(AUDIO_USER_INFO_ELEMENT);
		for (String key : audioUserInfo.keySet()) {
			Element itemElement = userInfoElement.addElement(ITEM_ELEMENT);
			UserDefaults.save(itemElement, audioUserInfo.get(key), key);
		}
		root.addElement(VOLUME_ELEMENT).addText(String.valueOf(volume.get()));

		// Paths
		root.addElement(CACHE_PATH_ELEMENT).addText(cachePath.toString());
		root.addElement(AUTO_UPDATE_ELEMENT).addText(String.valueOf(autoUpdate));

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
