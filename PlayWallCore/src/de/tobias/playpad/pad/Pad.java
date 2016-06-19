package de.tobias.playpad.pad;

import java.nio.file.Path;
import java.util.HashMap;

import org.dom4j.Element;

import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.Pauseable;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.pad.triggerlistener.PadTriggerContentListener;
import de.tobias.playpad.pad.triggerlistener.PadTriggerDurationListener;
import de.tobias.playpad.pad.triggerlistener.PadTriggerStatusListener;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.utils.settings.UserDefaults;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pad {

	private IntegerProperty indexProperty = new SimpleIntegerProperty();
	private StringProperty nameProperty = new SimpleStringProperty();
	private ObjectProperty<PadStatus> statusProperty = new SimpleObjectProperty<>(PadStatus.EMPTY);

	private ObjectProperty<PadContent> contentProperty = new SimpleObjectProperty<>();

	// Settings
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty loopProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<TimeMode> timeModeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Fade> fadeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Warning> warningProperty = new SimpleObjectProperty<>();

	private BooleanProperty customLayoutProperty = new SimpleBooleanProperty(false);
	private HashMap<String, CartLayout> layouts = new HashMap<>();

	private HashMap<String, Object> customSettings = new HashMap<>();

	// Trigger
	private HashMap<TriggerPoint, Trigger> triggers = new HashMap<>();

	// Custom Volume
	private transient DoubleProperty customVolumeProperty = new SimpleDoubleProperty(1.0);

	// Global Listener (unabhängig von der UI), für Core Functions wie Play, Pause
	private transient PadStatusListener padStatusListener;

	// Trigger Listener
	private transient PadTriggerStatusListener padTriggerStatusListener;
	private transient PadTriggerDurationListener padTriggerDurationListener;
	private transient PadTriggerContentListener padTriggerContentListener;
	private transient boolean ignoreTrigger = false;

	// Utils
	private transient boolean eof;
	private transient IPadViewController controller;
	private transient Project project;

	public Pad(Project project, Element element) {
		this.project = project;
		load(element);

		initPadListener();
		// Update Trigger ist nicht notwendig, da es in load(Element) ausgerufen wird
	}

	public Pad(Project project, int index) {
		this.project = project;
		setIndex(index);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		updateTrigger();
	}

	public Pad(Project project, int index, String name, PadContent content) {
		this(project, index);
		setName(name);
		setContent(content);
	}

	private void initPadListener() {
		padStatusListener = new PadStatusListener(this);
		statusProperty.addListener(padStatusListener);

		padTriggerStatusListener = new PadTriggerStatusListener(this);
		statusProperty.addListener(padTriggerStatusListener);

		padTriggerDurationListener = new PadTriggerDurationListener(this);

		// Das ist für die Position Listener notwendig, wenn sich der Content ändert
		padTriggerContentListener = new PadTriggerContentListener(this);
		padTriggerContentListener.changed(contentProperty, null, getContent());
	}

	// Accessor Methods
	public int getIndex() {
		return indexProperty.get();
	}

	public int getIndexReadable() {
		return indexProperty.get() + 1;
	}

	public void setIndex(int index) {
		this.indexProperty.set(index);
	}

	public ReadOnlyIntegerProperty indexProperty() {
		return indexProperty;
	}

	public String getName() {
		return nameProperty.get();
	}

	public void setName(String name) {
		this.nameProperty.set(name);
	}

	public StringProperty nameProperty() {
		return nameProperty;
	}

	public PadStatus getStatus() {
		return statusProperty.get();
	}

	public void setStatus(PadStatus status) {
		// PLay, Pause & Stop nut wenn Pad Content hat
		if (status == PadStatus.PLAY || status == PadStatus.STOP || status == PadStatus.PAUSE) {
			if (this.statusProperty.get() == PadStatus.EMPTY) {
				return;
			}
		}
		// Pause nur wenn Pause möglich
		if (status == PadStatus.PAUSE && !(getContent() instanceof Pauseable)) {
			return;
		}
		// Stop nicht wenn Ready (Stop/Keine Wiedergabe)
		if (status == PadStatus.STOP && getStatus() == PadStatus.READY) {
			return;
		}
		this.statusProperty.set(status);
	}

	public void setStatus(PadStatus status, boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
		setStatus(status);
	}

	public ObjectProperty<PadStatus> statusProperty() {
		return statusProperty;
	}

	public PadContent getContent() {
		return contentProperty.get();
	}

	public void setContent(PadContent content) {
		this.contentProperty.set(content);
	}

	public ObjectProperty<PadContent> contentProperty() {
		return contentProperty;
	}

	public double getVolume() {
		return volumeProperty.get();
	}

	public void setVolume(double volume) {
		volumeProperty.set(volume);
	}

	public void setMasterVolume(double volume) {
		if (getContent() != null) {
			getContent().setMasterVolume(volume);
		}
	}

	public DoubleProperty volumeProperty() {
		return volumeProperty;
	}

	public boolean isLoop() {
		return loopProperty.get();
	}

	public void setLoop(boolean loop) {
		this.loopProperty.set(loop);
	}

	public BooleanProperty loopProperty() {
		return loopProperty;
	}

	public boolean isCustomTimeMode() {
		return timeModeProperty.isNotNull().get();
	}

	public BooleanBinding customTimeModeProperty() {
		return timeModeProperty.isNotNull();
	}

	public TimeMode getTimeMode() {
		if (timeModeProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getPlayerTimeDisplayMode();
			}
		}
		return timeModeProperty.get();
	}

	public void setTimeMode(TimeMode timeMode) {
		this.timeModeProperty.set(timeMode);
	}

	public ObjectProperty<TimeMode> timeModeProperty() {
		return timeModeProperty;
	}

	public boolean isCustomFade() {
		return fadeProperty.isNotNull().get();
	}

	public BooleanBinding customFadeProperty() {
		return fadeProperty.isNotNull();
	}

	/**
	 * Returns either the fade settings of this pad or the global settings
	 * 
	 * @return
	 */
	public Fade getFade() {
		if (fadeProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getFade();
			}
		}
		return fadeProperty.get();
	}

	public void setFade(Fade fade) {
		this.fadeProperty.set(fade);
	}

	public ObjectProperty<Fade> fadeProperty() {
		return fadeProperty;
	}

	public boolean isCustomWarning() {
		return warningProperty.isNotNull().get();
	}

	public BooleanBinding customWarningProperty() {
		return warningProperty.isNotNull();
	}

	public Warning getWarning() {
		if (warningProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getWarningFeedback();
			}
		}
		return warningProperty.get();
	}

	public void setWarning(Warning warning) {
		this.warningProperty.set(warning);
	}

	public ObjectProperty<Warning> warningProperty() {
		return warningProperty;
	}

	public boolean isCustomLayout() {
		return customLayoutProperty.get();
	}

	public void setCustomLayout(boolean customLayout) {
		this.customLayoutProperty.set(customLayout);
	}

	public BooleanProperty customLayoutProperty() {
		return customLayoutProperty;
	}

	public CartLayout getLayout() {
		return getLayout(Profile.currentProfile().getProfileSettings().getLayoutType());
	}

	public CartLayout getLayout(String type) {
		if (!layouts.containsKey(type)) {
			layouts.put(type, LayoutRegistry.getLayout(type).newCartLayout());
		}
		return layouts.get(type);
	}

	public void setLayout(CartLayout layout, String type) {
		this.layouts.put(type, layout);
	}

	public boolean isEof() {
		return eof;
	}

	public void setEof(boolean eof) {
		this.eof = eof;
	}

	public HashMap<String, Object> getCustomSettings() {
		return customSettings;
	}

	public HashMap<TriggerPoint, Trigger> getTriggers() {
		return triggers;
	}

	public Trigger getTrigger(TriggerPoint point) {
		return triggers.get(point);
	}

	// Helper Methodes
	public void loadContent() {
		if (contentProperty.get() != null)
			contentProperty.get().loadMedia();
	}

	public void throwException(Path path, Exception exception) {
		if (project != null)
			project.addException(this, path, exception);
		setStatus(PadStatus.ERROR);
	}

	public void removeExceptionsForPad() {
		if (project != null)
			project.removeExceptions(this);
	}

	public void removeException(PadException exception) {
		if (project != null)
			project.removeException(exception);
	}

	public PadTriggerDurationListener getPadTriggerDurationListener() {
		return padTriggerDurationListener;
	}

	public boolean isIgnoreTrigger() {
		return ignoreTrigger;
	}

	public void setIgnoreTrigger(boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
	}

	private void updateTrigger() {
		for (TriggerPoint point : TriggerPoint.values()) {
			if (!triggers.containsKey(point)) {
				Trigger trigger = new Trigger(point);
				triggers.put(point, trigger);
			}
		}
	}

	public boolean hasTriggerItems() {
		for (Trigger trigger : triggers.values()) {
			if (!trigger.getItems().isEmpty())
				return true;
		}
		return false;
	}

	public Project getProject() {
		return project;
	}

	public boolean isPadVisible() {
		return controller != null;
	}

	public IPadViewController getController() {
		return controller;
	}

	public void setController(IPadViewController controller) {
		this.controller = controller;
	}

	public void clear() {
		setName("");
		if (contentProperty.isNotNull().get())
			contentProperty.get().unloadMedia();
		setContent(null);
		setStatus(PadStatus.EMPTY);

		if (project != null) {
			project.removeExceptions(this);
		}
	}

	// Storage
	private static final String INDEX_ATTR = "index";
	private static final String NAME_ATTR = "name";
	private static final String STATUS_ATTR = "status";

	private static final String SETTINGS_ELEMENT = "Settings";
	private static final String VOLUME_ELEMENT = "Volume";
	private static final String LOOP_ELEMENT = "Loop";
	private static final String TIMEMODE_ELEMENT = "TimeMode";
	private static final String FADE_ELEMENT = "Fade";
	private static final String WARNING_ELEMENT = "Warning";

	private static final String LAYOUTS_ELEMENT = "Layouts";
	private static final String LAYOUT_ACTIVE_ATTR = "active";
	private static final String LAYOUT_TYPE_ATTR = "type";
	private static final String LAYOUT_ELEMENT = "Layout";

	private static final String CUSTOM_SETTINGS_ITEM_ELEMENT = "Item";
	private static final String CUSTOM_SETTINGS_TYPE_ATTR = "key";
	private static final String CUSTOM_SETTINGS_ELEMENT = "CustomSettings";

	public static final String CONTENT_ELEMENT = "Content";
	private static final String CONTENT_TYPE_ATTR = "type";

	public void load(Element element) {
		indexProperty.set(Integer.valueOf(element.attributeValue(INDEX_ATTR)));
		nameProperty.set(element.attributeValue(NAME_ATTR));
		PadStatus status = PadStatus.valueOf(element.attributeValue(STATUS_ATTR));
		if (status == PadStatus.EMPTY || status == PadStatus.READY)
			statusProperty.set(status);

		// Settings
		Element settingsElement = element.element(SETTINGS_ELEMENT);
		if (settingsElement.element(VOLUME_ELEMENT) != null)
			volumeProperty.set(Double.valueOf(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			loopProperty.set(Boolean.valueOf(settingsElement.element(LOOP_ELEMENT).getStringValue()));
		if (settingsElement.element(TIMEMODE_ELEMENT) != null)
			timeModeProperty.set(TimeMode.valueOf(settingsElement.element(TIMEMODE_ELEMENT).getStringValue()));
		if (settingsElement.element(FADE_ELEMENT) != null)
			fadeProperty.set(Fade.load(settingsElement.element(FADE_ELEMENT)));
		if (settingsElement.element(WARNING_ELEMENT) != null)
			warningProperty.set(Warning.load(settingsElement.element(WARNING_ELEMENT)));

		// Laoyut
		Element layoutsElement = settingsElement.element(LAYOUTS_ELEMENT);
		if (layoutsElement != null) {
			if (layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR) != null) {
				customLayoutProperty.set(Boolean.valueOf(layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR)));
			}

			for (Object layoutObj : layoutsElement.elements(LAYOUT_ELEMENT)) {
				if (layoutObj instanceof Element) {
					Element layoutElement = (Element) layoutObj;
					String type = layoutElement.attributeValue(LAYOUT_TYPE_ATTR);
					CartLayout layout = LayoutRegistry.getLayout(type).newCartLayout();
					layout.load(layoutElement);

					layouts.put(type, layout);
				}
			}
		}

		Element userInfoElement = settingsElement.element(CUSTOM_SETTINGS_ELEMENT);
		if (userInfoElement != null) {
			for (Object object : userInfoElement.elements()) {
				if (object instanceof Element) {
					Element item = (Element) object;
					String key = item.attributeValue(CUSTOM_SETTINGS_TYPE_ATTR);
					Object data = UserDefaults.loadElement(item);
					customSettings.put(key, data);
				}
			}
		}

		// Trigger
		Element triggersElement = element.element("Triggers");
		if (triggersElement != null) {
			for (Object triggerObj : triggersElement.elements("Trigger")) {
				if (triggerObj instanceof Element) {
					Element triggerElement = (Element) triggerObj;
					Trigger trigger = new Trigger();
					trigger.load(triggerElement);
					triggers.put(trigger.getTriggerPoint(), trigger);
				}
			}
		}
		updateTrigger(); // Damit alle Points da sind

		// Content
		Element contentElement = element.element(CONTENT_ELEMENT);
		if (contentElement != null) {
			String contentType = contentElement.attributeValue(CONTENT_TYPE_ATTR);
			try {
				PadContent content = PadContentRegistry.getPadContentConnect(contentType).newInstance(this);
				content.load(contentElement);
				setContent(content);
			} catch (UnkownPadContentException e) {
				e.printStackTrace();
				throwException(null, e);
			}
		}
	}

	public void save(Element element) {
		element.addAttribute(INDEX_ATTR, String.valueOf(indexProperty.get()));
		element.addAttribute(NAME_ATTR, nameProperty.get());
		if (statusProperty.get() == PadStatus.EMPTY || statusProperty.get() == PadStatus.ERROR) {
			element.addAttribute(STATUS_ATTR, PadStatus.EMPTY.name());
		} else {
			element.addAttribute(STATUS_ATTR, PadStatus.READY.name());
		}

		// Settings
		Element settingsElement = element.addElement(SETTINGS_ELEMENT);
		settingsElement.addElement(VOLUME_ELEMENT).addText(String.valueOf(volumeProperty.get()));
		settingsElement.addElement(LOOP_ELEMENT).addText(String.valueOf(loopProperty.get()));
		if (timeModeProperty.isNotNull().get())
			settingsElement.addElement(TIMEMODE_ELEMENT).addText(String.valueOf(timeModeProperty.get()));
		if (warningProperty.isNotNull().get())
			warningProperty.get().save(settingsElement.addElement(WARNING_ELEMENT));
		if (fadeProperty.isNotNull().get())
			fadeProperty.get().save(settingsElement.addElement(FADE_ELEMENT));

		// Layout
		Element layoutsElement = settingsElement.addElement(LAYOUTS_ELEMENT);
		layoutsElement.addAttribute(LAYOUT_ACTIVE_ATTR, String.valueOf(customLayoutProperty.get()));
		for (String layoutType : layouts.keySet()) {
			Element layoutElement = layoutsElement.addElement(LAYOUT_ELEMENT);
			layoutElement.addAttribute(LAYOUT_TYPE_ATTR, layoutType);

			CartLayout cartLayout = layouts.get(layoutType);
			cartLayout.save(layoutElement);
		}

		Element userInfoElement = settingsElement.addElement(CUSTOM_SETTINGS_ELEMENT);
		for (String key : customSettings.keySet()) {
			Element itemElement = userInfoElement.addElement(CUSTOM_SETTINGS_ITEM_ELEMENT);
			UserDefaults.save(itemElement, customSettings.get(key), key);
		}

		// Trigger
		Element triggersElement = element.addElement("Triggers");
		for (TriggerPoint point : triggers.keySet()) {
			Trigger trigger = triggers.get(point);
			Element triggerElement = triggersElement.addElement("Trigger");
			trigger.save(triggerElement);
		}

		// Content
		if (contentProperty.get() != null) {
			Element contentElement = element.addElement(CONTENT_ELEMENT);
			contentElement.addAttribute(CONTENT_TYPE_ATTR, contentProperty.get().getType());
			contentProperty.get().save(contentElement);
		}
	}

	@Override
	public String toString() {
		return "Pad: " + indexProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (indexProperty.get() + 1) + " - " + nameProperty.get();
	}

	// TODO Reorder
	public void setCustomVolume(double volume) {
		customVolumeProperty.set(volume);
	}

	public double getCustomVolume() {
		return customVolumeProperty.get();
	}

	public DoubleProperty customVolumeProperty() {
		return customVolumeProperty;
	}
}
