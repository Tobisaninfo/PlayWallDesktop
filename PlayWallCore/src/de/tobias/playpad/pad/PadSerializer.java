package de.tobias.playpad.pad;

import org.dom4j.Element;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.project.Project;
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

	// TODO Remove project var
	private Project project;

	public PadSerializer(Project project) {
		this.project = project;
	}

	public PadSerializer() {}

	@Override
	public Pad loadElement(Element element) {
		Pad pad = new Pad(project);

		pad.setIndex(Integer.valueOf(element.attributeValue(INDEX_ATTR)));
		pad.setName(element.attributeValue(NAME_ATTR));
		PadStatus status = PadStatus.valueOf(element.attributeValue(STATUS_ATTR));
		if (status == PadStatus.EMPTY || status == PadStatus.READY)
			pad.setStatus(status);

		// Settings
		Element settingsElement = element.element(SETTINGS_ELEMENT);
		if (settingsElement.element(VOLUME_ELEMENT) != null)
			pad.setVolume(Double.valueOf(settingsElement.element(VOLUME_ELEMENT).getStringValue()));
		if (settingsElement.element(LOOP_ELEMENT) != null)
			pad.setLoop(Boolean.valueOf(settingsElement.element(LOOP_ELEMENT).getStringValue()));
		if (settingsElement.element(TIMEMODE_ELEMENT) != null)
			pad.setTimeMode(TimeMode.valueOf(settingsElement.element(TIMEMODE_ELEMENT).getStringValue()));
		if (settingsElement.element(FADE_ELEMENT) != null)
			pad.setFade(Fade.load(settingsElement.element(FADE_ELEMENT)));
		if (settingsElement.element(WARNING_ELEMENT) != null)
			pad.setWarning(Warning.load(settingsElement.element(WARNING_ELEMENT)));

		// Laoyut
		Element layoutsElement = settingsElement.element(LAYOUTS_ELEMENT);
		if (layoutsElement != null) {
			if (layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR) != null) {
				pad.setCustomLayout(Boolean.valueOf(layoutsElement.attributeValue(LAYOUT_ACTIVE_ATTR)));
			}

			for (Object layoutObj : layoutsElement.elements(LAYOUT_ELEMENT)) {
				if (layoutObj instanceof Element) {
					Element layoutElement = (Element) layoutObj;
					String type = layoutElement.attributeValue(LAYOUT_TYPE_ATTR);

					try {
						DefaultRegistry<LayoutConnect> layouts = PlayPadPlugin.getRegistryCollection().getLayouts();
						CartLayout layout = layouts.getComponent(type).newCartLayout();
						layout.load(layoutElement);

						pad.setLayout(layout, type);
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
					pad.getCustomSettings().put(key, data);
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
					pad.getTriggers().put(trigger.getTriggerPoint(), trigger);
				}
			}
		}
		pad.updateTrigger(); // Damit alle Points da sind

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
				pad.throwException(null, e);
			}
		}

		return pad;
	}

	@Override
	public void saveElement(Element element, Pad data) {
		element.addAttribute(INDEX_ATTR, String.valueOf(data.getIndex()));
		element.addAttribute(NAME_ATTR, data.getName());
		if (data.getStatus() == PadStatus.EMPTY || data.getStatus() == PadStatus.ERROR) {
			element.addAttribute(STATUS_ATTR, PadStatus.EMPTY.name());
		} else {
			element.addAttribute(STATUS_ATTR, PadStatus.READY.name());
		}

		// Settings
		Element settingsElement = element.addElement(SETTINGS_ELEMENT);
		settingsElement.addElement(VOLUME_ELEMENT).addText(String.valueOf(data.getVolume()));
		settingsElement.addElement(LOOP_ELEMENT).addText(String.valueOf(data.isLoop()));
		if (data.getTimeMode() != null)
			settingsElement.addElement(TIMEMODE_ELEMENT).addText(String.valueOf(data.getTimeMode()));
		if (data.isCustomWarning() != false)
			data.getWarning().save(settingsElement.addElement(WARNING_ELEMENT));
		if (data.getFade() != null)
			data.getFade().save(settingsElement.addElement(FADE_ELEMENT));

		// Layout
		Element layoutsElement = settingsElement.addElement(LAYOUTS_ELEMENT);
		layoutsElement.addAttribute(LAYOUT_ACTIVE_ATTR, String.valueOf(data.isCustomLayout()));
		for (String layoutType : data.getLayouts().keySet()) {
			Element layoutElement = layoutsElement.addElement(LAYOUT_ELEMENT);
			layoutElement.addAttribute(LAYOUT_TYPE_ATTR, layoutType);

			CartLayout cartLayout = data.getLayouts().get(layoutType);
			cartLayout.save(layoutElement);
		}

		Element userInfoElement = settingsElement.addElement(CUSTOM_SETTINGS_ELEMENT);
		for (String key : data.getCustomSettings().keySet()) {
			Element itemElement = userInfoElement.addElement(CUSTOM_SETTINGS_ITEM_ELEMENT);
			UserDefaults.save(itemElement, data.getCustomSettings().get(key), key);
		}

		// Trigger
		Element triggersElement = element.addElement("Triggers");
		for (TriggerPoint point : data.getTriggers().keySet()) {
			Trigger trigger = data.getTriggers().get(point);
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
