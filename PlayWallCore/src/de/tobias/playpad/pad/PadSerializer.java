package de.tobias.playpad.pad;

import java.util.UUID;

import org.dom4j.Element;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.settings.Warning;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.xml.XMLDeserializer;
import de.tobias.playpad.xml.XMLSerializer;
import de.tobias.utils.settings.UserDefaults;

public class PadSerializer implements XMLSerializer<Pad>, XMLDeserializer<Pad> {

	private static final String UUID_ATTR = "uuid";
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

	private ProjectV2 project;

	public PadSerializer(ProjectV2 project) {
		this.project = project;
	}

	public PadSerializer() {
	}

	@Override
	public Pad loadElement(Element element) {
		Pad pad = new Pad(project);

		if (element.attributeValue(UUID_ATTR) != null)
			pad.setUuid(UUID.fromString(element.attributeValue(UUID_ATTR)));
		pad.setIndex(Integer.valueOf(element.attributeValue(INDEX_ATTR)));
		pad.setName(element.attributeValue(NAME_ATTR));
		PadStatus status = PadStatus.valueOf(element.attributeValue(STATUS_ATTR));
		if (status == PadStatus.EMPTY || status == PadStatus.READY)
			pad.setStatus(status);

		// Settings
		Element settingsElement = element.element(SETTINGS_ELEMENT);
		PadSettings padSettings = pad.getPadSettings();

		if (settingsElement.element(VOLUME_ELEMENT) != null)
			padSettings.setVolume(Double.valueOf(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			padSettings.setLoop(Boolean.valueOf(settingsElement.element(LOOP_ELEMENT).getStringValue()));
		if (settingsElement.element(TIMEMODE_ELEMENT) != null)
			padSettings.setTimeMode(TimeMode.valueOf(settingsElement.element(TIMEMODE_ELEMENT).getStringValue()));
		if (settingsElement.element(FADE_ELEMENT) != null)
			padSettings.setFade(Fade.load(settingsElement.element(FADE_ELEMENT)));
		if (settingsElement.element(WARNING_ELEMENT) != null)
			padSettings.setWarning(Warning.load(settingsElement.element(WARNING_ELEMENT)));

		// Laoyut
		Element layoutsElement = settingsElement.element(LAYOUTS_ELEMENT);
		if (layoutsElement != null) {
			if (layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR) != null) {
				padSettings.setCustomLayout(Boolean.valueOf(layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR)));
			}

			for (Object layoutObj : layoutsElement.elements(LAYOUT_ELEMENT)) {
				if (layoutObj instanceof Element) {
					Element layoutElement = (Element) layoutObj;
					String type = layoutElement.attributeValue(LAYOUT_TYPE_ATTR);

					try {
						DefaultRegistry<DesignConnect> layouts = PlayPadPlugin.getRegistryCollection().getDesigns();
						CartDesign layout = layouts.getComponent(type).newCartDesign();
						layout.load(layoutElement);

						padSettings.setLayout(layout, type);
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
		Element triggersElement = element.element("Triggers"); // TODO Externalize
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

		// Content
		Element contentElement = element.element(CONTENT_ELEMENT);
		if (contentElement != null) {
			String contentType = contentElement.attributeValue(CONTENT_TYPE_ATTR);
			try {
				Registry<PadContentConnect> padContents = PlayPadPlugin.getRegistryCollection().getPadContents();
				PadContent content = padContents.getComponent(contentType).newInstance(pad);

				content.load(contentElement);
				pad.setContent(content);
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// pad.throwException(null, e); TODO Throw exception to user
			}
		}

		return pad;
	}

	@Override
	public void saveElement(Element element, Pad data) {
		element.addAttribute(UUID_ATTR, data.getUuid().toString());
		element.addAttribute(INDEX_ATTR, String.valueOf(data.getIndex()));
		element.addAttribute(NAME_ATTR, data.getName());
		if (data.getStatus() == PadStatus.EMPTY || data.getStatus() == PadStatus.ERROR) {
			element.addAttribute(STATUS_ATTR, PadStatus.EMPTY.name());
		} else {
			element.addAttribute(STATUS_ATTR, PadStatus.READY.name());
		}

		// Settings
		Element settingsElement = element.addElement(SETTINGS_ELEMENT);
		PadSettings padSettings = data.getPadSettings();

		settingsElement.addElement(VOLUME_ELEMENT).addText(String.valueOf(padSettings.getVolume()));
		settingsElement.addElement(LOOP_ELEMENT).addText(String.valueOf(padSettings.isLoop()));
		if (padSettings.getTimeMode() != null)
			settingsElement.addElement(TIMEMODE_ELEMENT).addText(String.valueOf(padSettings.getTimeMode()));
		if (padSettings.isCustomWarning() != false)
			padSettings.getWarning().save(settingsElement.addElement(WARNING_ELEMENT));
		if (padSettings.getFade() != null)
			padSettings.getFade().save(settingsElement.addElement(FADE_ELEMENT));

		// Layout
		Element layoutsElement = settingsElement.addElement(LAYOUTS_ELEMENT);
		layoutsElement.addAttribute(LAYOUT_ACTIVE_ATTR, String.valueOf(padSettings.isCustomLayout()));
		for (String layoutType : padSettings.getLayouts().keySet()) {
			Element layoutElement = layoutsElement.addElement(LAYOUT_ELEMENT);
			layoutElement.addAttribute(LAYOUT_TYPE_ATTR, layoutType);

			CartDesign cartLayout = padSettings.getLayouts().get(layoutType);
			cartLayout.save(layoutElement);
		}

		Element userInfoElement = settingsElement.addElement(CUSTOM_SETTINGS_ELEMENT);
		for (String key : padSettings.getCustomSettings().keySet()) {
			Element itemElement = userInfoElement.addElement(CUSTOM_SETTINGS_ITEM_ELEMENT);
			UserDefaults.save(itemElement, padSettings.getCustomSettings().get(key), key);
		}

		// Trigger
		Element triggersElement = element.addElement("Triggers");
		for (TriggerPoint point : padSettings.getTriggers().keySet()) {
			Trigger trigger = padSettings.getTriggers().get(point);
			Element triggerElement = triggersElement.addElement("Trigger");
			trigger.save(triggerElement);
		}

		// Content
		if (data.getContent() != null) {
			Element contentElement = element.addElement(CONTENT_ELEMENT);
			contentElement.addAttribute(CONTENT_TYPE_ATTR, data.getContent().getType());
			data.getContent().save(contentElement);
		}
	}
}
