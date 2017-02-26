package de.tobias.playpad.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
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

	public PadSettings loadElement(Element settingsElement, Pad pad) {
		PadSettings padSettings = new PadSettings(pad);

		if (settingsElement.element(VOLUME_ELEMENT) != null)
			padSettings.setVolume(Double.valueOf(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			padSettings.setLoop(Boolean.valueOf(settingsElement.element(LOOP_ELEMENT).getStringValue()));
		if (settingsElement.element(TIMEMODE_ELEMENT) != null)
			padSettings.setTimeMode(TimeMode.valueOf(settingsElement.element(TIMEMODE_ELEMENT).getStringValue()));
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
		Element layoutsElement = settingsElement.element(LAYOUTS_ELEMENT);
		if (layoutsElement != null) {
			if (layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR) != null) {
				padSettings.setCustomDesign(Boolean.valueOf(layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR)));
			}

			for (Object layoutObj : layoutsElement.elements(LAYOUT_ELEMENT)) {
				if (layoutObj instanceof Element) {
					Element layoutElement = (Element) layoutObj;
					String type = layoutElement.attributeValue(LAYOUT_TYPE_ATTR);

					try {
						DefaultRegistry<DesignFactory> layouts = PlayPadPlugin.getRegistryCollection().getDesigns();
						CartDesign layout = layouts.getFactory(type).newCartDesign(pad);
						layout.load(layoutElement);

						padSettings.setDesign(layout, type);
					} catch (NoSuchComponentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
					padSettings.getCustomSettings().put(key, data);
				}
			}
		}

		// Trigger
		Element triggersElement = settingsElement.element("Triggers"); // TODO Externalize
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
			settingsElement.addElement(TIMEMODE_ELEMENT).addText(String.valueOf(padSettings.getTimeMode()));
		if (padSettings.isCustomWarning())
			settingsElement.addElement(WARNING_ELEMENT).addText(padSettings.getWarning().toString());
		if (padSettings.isCustomFade())
			padSettings.getFade().save(settingsElement.addElement(FADE_ELEMENT));

		// Layout
		Element layoutsElement = settingsElement.addElement(LAYOUTS_ELEMENT);
		layoutsElement.addAttribute(LAYOUT_ACTIVE_ATTR, String.valueOf(padSettings.isCustomDesign()));
		for (String layoutType : padSettings.getDesigns().keySet()) {
			Element layoutElement = layoutsElement.addElement(LAYOUT_ELEMENT);
			layoutElement.addAttribute(LAYOUT_TYPE_ATTR, layoutType);

			CartDesign cartLayout = padSettings.getDesigns().get(layoutType);
			cartLayout.save(layoutElement);
		}

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
