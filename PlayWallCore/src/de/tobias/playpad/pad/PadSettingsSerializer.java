package de.tobias.playpad.pad;

import de.tobias.playpad.design.modern.ModernCartDesign2;
import de.tobias.playpad.design.modern.serializer.ModernCartDesignSerializer;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.utils.settings.UserDefaults;
import javafx.util.Duration;
import org.dom4j.Element;

/**
 * Created by tobias on 26.02.17.
 */
public class PadSettingsSerializer {

	private static final String VOLUME_ELEMENT = "Volume";
	private static final String LOOP_ELEMENT = "Loop";
	private static final String TIME_MODE_ELEMENT = "TimeMode";
	private static final String FADE_ELEMENT = "Fade";
	private static final String WARNING_ELEMENT = "Warning";

	private static final String DESIGN_ELEMENT = "Design";
	private static final String CUSTOM_DESIGN_ELEMENT = "custom";

	private static final String CUSTOM_SETTINGS_ITEM_ELEMENT = "Item";
	private static final String CUSTOM_SETTINGS_TYPE_ATTR = "key";
	private static final String CUSTOM_SETTINGS_ELEMENT = "CustomSettings";

	public PadSettings loadElement(Element settingsElement, Pad pad) {
		PadSettings padSettings = new PadSettings(pad);

		if (settingsElement.element(VOLUME_ELEMENT) != null)
			padSettings.setVolume(Double.valueOf(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			padSettings.setLoop(Boolean.valueOf(settingsElement.element(LOOP_ELEMENT).getStringValue()));
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

		// Layout
		Element designElement = settingsElement.element(DESIGN_ELEMENT);
		if (designElement != null) {
			if (designElement.attributeValue(CUSTOM_DESIGN_ELEMENT) != null) {
				padSettings.setCustomDesign(Boolean.valueOf(designElement.attributeValue(CUSTOM_DESIGN_ELEMENT)));
			}
			ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
			ModernCartDesign2 design = serializer.load(designElement, pad);
			padSettings.setDesign(design);
		}

		Element userInfoElement = settingsElement.element(CUSTOM_SETTINGS_ELEMENT);
		if (userInfoElement != null) {
			for (Object object : userInfoElement.elements()) {
				if (object instanceof Element) {
					Element item = (Element) object;
					String key = item.attributeValue(CUSTOM_SETTINGS_TYPE_ATTR);
					Object data = UserDefaults.loadElement(item);
					padSettings.getCustomSettings().put(key, data);
				}
			}
		}

		// Trigger
		Element triggersElement = settingsElement.element("Triggers");
		if (triggersElement != null) {
			for (Object triggerObj : triggersElement.elements("Trigger")) {
				if (triggerObj instanceof Element) {
					Element triggerElement = (Element) triggerObj;
					Trigger trigger = new Trigger();
					trigger.load(triggerElement);
					padSettings.getTriggers().put(trigger.getTriggerPoint(), trigger);
				}
			}
		}
		padSettings.updateTrigger(); // Damit alle Points da sind

		return padSettings;
	}

	public void saveElement(Element settingsElement, PadSettings padSettings) {
		// Settings
		settingsElement.addElement(VOLUME_ELEMENT).addText(String.valueOf(padSettings.getVolume()));
		settingsElement.addElement(LOOP_ELEMENT).addText(String.valueOf(padSettings.isLoop()));
		if (padSettings.isCustomTimeMode())
			settingsElement.addElement(TIME_MODE_ELEMENT).addText(String.valueOf(padSettings.getTimeMode()));
		if (padSettings.isCustomWarning())
			settingsElement.addElement(WARNING_ELEMENT).addText(padSettings.getWarning().toString());
		if (padSettings.isCustomFade())
			padSettings.getFade().save(settingsElement.addElement(FADE_ELEMENT));

		// Layout
		Element designElement = settingsElement.addElement(DESIGN_ELEMENT);
		ModernCartDesignSerializer serializer = new ModernCartDesignSerializer();
		serializer.save(designElement, padSettings.getDesign());
		designElement.addAttribute(CUSTOM_DESIGN_ELEMENT, String.valueOf(padSettings.isCustomDesign()));

		Element userInfoElement = settingsElement.addElement(CUSTOM_SETTINGS_ELEMENT);
		for (String key : padSettings.getCustomSettings().keySet()) {
			Element itemElement = userInfoElement.addElement(CUSTOM_SETTINGS_ITEM_ELEMENT);
			UserDefaults.save(itemElement, padSettings.getCustomSettings().get(key), key);
		}

		// Trigger
		Element triggersElement = settingsElement.addElement("Triggers");
		for (TriggerPoint point : padSettings.getTriggers().keySet()) {
			Trigger trigger = padSettings.getTriggers().get(point);
			Element triggerElement = triggersElement.addElement("Trigger");
			trigger.save(triggerElement);
		}
	}
}
