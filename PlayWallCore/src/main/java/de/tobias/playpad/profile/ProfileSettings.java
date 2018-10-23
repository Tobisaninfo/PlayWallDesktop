package de.tobias.playpad.profile;

import de.thecodelabs.storage.settings.UserDefaults;
import de.thecodelabs.storage.settings.annotation.Key;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.design.modern.serializer.ModernGlobalDesignSerializer;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.settings.Fade;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;
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
import java.util.HashMap;

public class ProfileSettings {

	private static final long serialVersionUID = 1L;

	@Key
	private BooleanProperty lockedProperty = new SimpleBooleanProperty(false);

	// MIDI
	@Key
	private String midiDevice;
	@Key
	private boolean midiActive = false;

	// Audio Output
	@Key
	private String audioClass = PlayPadPlugin.getRegistryCollection().getAudioHandlers().getDefaultID();
	@Key
	private HashMap<String, Object> audioUserInfo = new HashMap<>();

	// Layout
	@Key
	private ModernGlobalDesign design = new ModernGlobalDesign();
	@Key
	private String mainLayoutType = PlayPadPlugin.getRegistryCollection().getMainLayouts().getDefaultID();

	// Cart Settings
	@Key
	private boolean multiplePlayer = true;
	@Key
	private Duration warningTime = Duration.seconds(5);

	@Key
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);

	@Key
	private boolean windowAlwaysOnTop = false;

	@Key
	private Fade fade = new Fade();
	@Key
	private TimeMode player_timeDisplayMode = TimeMode.REST;

	public boolean isLocked() {
		return lockedProperty.get();
	}

	public void setLocked(boolean locked) {
		this.lockedProperty.set(locked);
	}

	public BooleanProperty lockedProperty() {
		return lockedProperty;
	}

	// Getter
	public String getMidiDevice() {
		return midiDevice;
	}

	public String getMainLayoutType() {
		return mainLayoutType;
	}

	public ModernGlobalDesign getDesign() {
		return design;
	}

	public Duration getWarningFeedback() {
		return warningTime;
	}

	public boolean isMidiActive() {
		return midiActive;
	}

	public double getVolume() {
		return volumeProperty.get();
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

	public String getAudioClass() {
		return audioClass;
	}

	public HashMap<String, Object> getAudioUserInfo() {
		return audioUserInfo;
	}

	public boolean isMultiplePlayer() {
		return multiplePlayer;
	}

	// Setter
	public void setMidiDeviceName(String midiDevice) {
		this.midiDevice = midiDevice;
	}

	public void setMainLayoutType(String mainLayoutType) {
		this.mainLayoutType = mainLayoutType;
	}

	public void setDesign(ModernGlobalDesign design) {
		this.design = design;
	}

	public void setWarningTime(Duration warningTime) {
		this.warningTime = warningTime;
	}

	public void setMidiActive(boolean midiActive) {
		this.midiActive = midiActive;
	}

	public void setVolume(double volume) {
		this.volumeProperty.set(volume);
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

	public void setAudioClass(String audioClass) {
		this.audioClass = audioClass;
	}

	public void setMultiplePlayer(boolean multiplePlayer) {
		this.multiplePlayer = multiplePlayer;
	}

	// Properties
	public DoubleProperty volumeProperty() {
		return volumeProperty;
	}

	private static final String LOCKED_ELEMENT = "Locked";
	private static final String ITEM_ELEMENT = "Item";
	private static final String VOLUME_ELEMENT = "Volume";
	private static final String KEY_ATTRIBUTE = "key";
	private static final String AUDIO_USER_INFO_ELEMENT = "AudioUserInfo";
	private static final String AUDIO_CLASS_ELEMENT = "AudioClass";
	private static final String WINDOW_ALWAYS_ON_TOP_ELEMENT = "WindowAlwaysOnTop";
	private static final String MULTIPLE_PLAYER_ELEMENT = "MultiplePlayer";
	private static final String TIME_DISPLAY_ELEMENT = "TimeDisplay";
	private static final String FADE_ELEMENT = "Fade";
	private static final String WARNING_ELEMENT = "Warning";
	private static final String MAIN_LAYOUT_TYPE_ELEMENT = "MainLayoutType";
	private static final String DESIGN_ELEMENT = "Design";
	private static final String MIDI_ACTIVE_ELEMENT = "MidiActive";
	private static final String MIDI_DEVICE_ELEMENT = "MidiDevice";

	// File Handler
	public static ProfileSettings load(Path path) throws DocumentException, IOException {
		ProfileSettings profileSettings = new ProfileSettings();

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			if (root.element(LOCKED_ELEMENT) != null)
				profileSettings.setLocked(Boolean.valueOf(root.element(LOCKED_ELEMENT).getStringValue()));
			if (root.element(MIDI_DEVICE_ELEMENT) != null)
				profileSettings.setMidiDeviceName(root.element(MIDI_DEVICE_ELEMENT).getStringValue());
			if (root.element(MIDI_ACTIVE_ELEMENT) != null)
				profileSettings.setMidiActive(Boolean.valueOf(root.element(MIDI_ACTIVE_ELEMENT).getStringValue()));

			if (root.element(DESIGN_ELEMENT) != null) {
				Element element = root.element(DESIGN_ELEMENT);
				ModernGlobalDesignSerializer serializer = new ModernGlobalDesignSerializer();
				profileSettings.setDesign(serializer.load(element));
			}
			if (root.element(MAIN_LAYOUT_TYPE_ELEMENT) != null) {
				profileSettings.setMainLayoutType(root.element(MAIN_LAYOUT_TYPE_ELEMENT).getStringValue());
			}

			if (root.element(MULTIPLE_PLAYER_ELEMENT) != null) {
				profileSettings.setMultiplePlayer(Boolean.valueOf(root.element(MULTIPLE_PLAYER_ELEMENT).getStringValue()));
			}

			if (root.element(WARNING_ELEMENT) != null) {
				try {
					Duration duration = Duration.valueOf(root.element(WARNING_ELEMENT).getStringValue().replace(" ", ""));
					profileSettings.setWarningTime(duration);
				} catch (Exception e) {
					profileSettings.setWarningTime(Duration.seconds(5));
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

			if (root.element(WINDOW_ALWAYS_ON_TOP_ELEMENT) != null)
				profileSettings.setWindowAlwaysOnTop(Boolean.valueOf(root.element(WINDOW_ALWAYS_ON_TOP_ELEMENT).getStringValue()));
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
		}
		return profileSettings;
	}

	public void save(Path path) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Config");

		root.addElement(LOCKED_ELEMENT).addText(String.valueOf(lockedProperty.get()));

		// MIDI
		if (midiDevice != null)
			root.addElement(MIDI_DEVICE_ELEMENT).addText(midiDevice);
		root.addElement(MIDI_ACTIVE_ELEMENT).addText(String.valueOf(midiActive));

		Element designElement = root.addElement(DESIGN_ELEMENT);
		ModernGlobalDesignSerializer serializer = new ModernGlobalDesignSerializer();
		serializer.save(designElement, design);
		root.addElement(MAIN_LAYOUT_TYPE_ELEMENT).addText(mainLayoutType);

		root.addElement(WARNING_ELEMENT).addText(warningTime.toString());
		fade.save(root.addElement(FADE_ELEMENT));
		root.addElement(TIME_DISPLAY_ELEMENT).addText(player_timeDisplayMode.name());

		root.addElement(MULTIPLE_PLAYER_ELEMENT).addText(String.valueOf(multiplePlayer));

		root.addElement(WINDOW_ALWAYS_ON_TOP_ELEMENT).addText(String.valueOf(windowAlwaysOnTop));

		// Audio
		root.addElement(AUDIO_CLASS_ELEMENT).addText(audioClass);
		Element userInfoElement = root.addElement(AUDIO_USER_INFO_ELEMENT);
		for (String key : audioUserInfo.keySet()) {
			Element itemElement = userInfoElement.addElement(ITEM_ELEMENT);
			UserDefaults.save(itemElement, audioUserInfo.get(key), key);
		}
		root.addElement(VOLUME_ELEMENT).addText(String.valueOf(volumeProperty.get()));

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
