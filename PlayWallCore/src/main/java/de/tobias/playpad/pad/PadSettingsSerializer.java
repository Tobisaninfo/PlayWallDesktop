package de.tobias.playpad.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.settings.UserDefaults;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.serializer.ModernCartDesignSerializer;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import javafx.util.Duration;
import org.dom4j.Element;

import java.util.UUID;

/**
 * Created by tobias on 26.02.17.
 */
public class PadSettingsSerializer {

	private static final String ID_ATTR = "id";

	private static final String VOLUME_ELEMENT = "Volume";
	private static final String LOOP_ELEMENT = "Loop";
	private static final String TIME_MODE_ELEMENT = "TimeMode";
	private static final String FADE_ELEMENT = "Fade";
	private static final String WARNING_ELEMENT = "Warning";
	private static final String CUE_IN_ELEMENT = "CueIn";

	private static final String DESIGN_ELEMENT = "Design";
	private static final String CUSTOM_DESIGN_ELEMENT = "custom";

	private static final String CUSTOM_SETTINGS_ITEM_ELEMENT = "Item";
	private static final String CUSTOM_SETTINGS_TYPE_ATTR = "key";
	private static final String CUSTOM_SETTINGS_ELEMENT = "CustomSettings";
	private static final String TRIGGERS_ELEMENT = "Triggers";
	private static final String TRIGGER_ELEMENT = "Trigger";

	public PadSettings loadElement(Element settingsElement, Pad pad) {
		PadSettings padSettings;
		if (settingsElement.attributeValue(ID_ATTR) != null) {
			UUID id = UUID.fromString(settingsElement.attributeValue(ID_ATTR));
			padSettings = new PadSettings(pad, id);
		} else {
			padSettings = new PadSettings(pad);
		}

		if (settingsElement.element(VOLUME_ELEMENT) != null)
			padSettings.setVolume(Double.parseDouble(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			padSettings.setLoop(Boolean.parseBoolean(settingsElement.element(LOOP_ELEMENT).getStringValue()));
		if (settingsElement.element(TIME_MODE_ELEMENT) != null)
			padSettings.setTimeMode(TimeMode.valueOf(settingsElement.element(TIME_MODE_ELEMENT).getStringValue()));
		if (settingsElement.element(FADE_ELEMENT) != null)
			padSettings.setFade(Fade.load(settingsElement.element(FADE_ELEMENT)));
		if (settingsElement.element(WARNING_ELEMENT) != null) {
			try {
				Duration duration = Duration.valueOf(settingsElement.element(WARNING_ELEMENT).getStringValue().replace(" ", ""));
				padSettings.setWarning(duration);
			} catch (Exception e) {
				padSettings.setWarning(Duration.seconds(5));
			}
		}
		if (settingsElement.element(CUE_IN_ELEMENT) != null) {
			try {
				Duration duration = Duration.valueOf(settingsElement.element(CUE_IN_ELEMENT).getStringValue().replace(" ", ""));
				padSettings.setCueIn(duration);
			} catch (Exception e) {
				padSettings.setCueIn(null);
			}
		}

		// Layout
		Element designElement = settingsElement.element(DESIGN_ELEMENT);
		if (designElement != null) {
			if (designElement.attributeValue(CUSTOM_DESIGN_ELEMENT) != null) {
				padSettings.setCustomDesign(Boolean.parseBoolean(designElement.attributeValue(CUSTOM_DESIGN_ELEMENT)));
			}
			ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
			ModernCartDesign design = serializer.load(designElement, pad);
			padSettings.setDesign(design);
		}

		Element userInfoElement = settingsElement.element(CUSTOM_SETTINGS_ELEMENT);
		if (userInfoElement != null) {
			for (Element item : userInfoElement.elements()) {
				String key = item.attributeValue(CUSTOM_SETTINGS_TYPE_ATTR);
				Object data = UserDefaults.loadElement(item);
				padSettings.getCustomSettings().put(key, data);
			}
		}

		// Trigger
		Element triggersElement = settingsElement.element(TRIGGERS_ELEMENT);
		if (triggersElement != null) {
			for (Element triggerElement : triggersElement.elements(TRIGGER_ELEMENT)) {
				try {
					Trigger trigger = new Trigger();
					trigger.load(triggerElement);
					padSettings.getTriggers().put(trigger.getTriggerPoint(), trigger);
				} catch (IllegalArgumentException e) {
					Logger.error(e);
				}
			}
		}
		padSettings.updateTrigger(); // Add missing trigger points

		return padSettings;
	}

	public void saveElement(Element settingsElement, PadSettings padSettings) {
		settingsElement.addAttribute(ID_ATTR, padSettings.getId().toString());

		settingsElement.addElement(VOLUME_ELEMENT).addText(String.valueOf(padSettings.getVolume()));
		settingsElement.addElement(LOOP_ELEMENT).addText(String.valueOf(padSettings.isLoop()));
		if (padSettings.isCustomTimeMode())
			settingsElement.addElement(TIME_MODE_ELEMENT).addText(String.valueOf(padSettings.getTimeMode()));
		if (padSettings.isCustomWarning())
			settingsElement.addElement(WARNING_ELEMENT).addText(padSettings.getWarning().toString());
		if (padSettings.isCustomFade())
			padSettings.getFade().save(settingsElement.addElement(FADE_ELEMENT));

		if (padSettings.getCueIn() != null)
			settingsElement.addElement(CUE_IN_ELEMENT).addText(padSettings.getCueIn().toString());

		// Layout
		Element designElement = settingsElement.addElement(DESIGN_ELEMENT);
		if (padSettings.isCustomDesign()) {
			ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
			serializer.save(designElement, padSettings.getDesign());
		}
		designElement.addAttribute(CUSTOM_DESIGN_ELEMENT, String.valueOf(padSettings.isCustomDesign()));

		Element userInfoElement = settingsElement.addElement(CUSTOM_SETTINGS_ELEMENT);
		for (String key : padSettings.getCustomSettings().keySet()) {
			Element itemElement = userInfoElement.addElement(CUSTOM_SETTINGS_ITEM_ELEMENT);
			UserDefaults.save(itemElement, padSettings.getCustomSettings().get(key), key);
		}

		// Trigger
		Element triggersElement = settingsElement.addElement(TRIGGERS_ELEMENT);
		for (TriggerPoint point : padSettings.getTriggers().keySet()) {
			Trigger trigger = padSettings.getTriggers().get(point);
			Element triggerElement = triggersElement.addElement(TRIGGER_ELEMENT);
			trigger.save(triggerElement);
		}
	}
}
