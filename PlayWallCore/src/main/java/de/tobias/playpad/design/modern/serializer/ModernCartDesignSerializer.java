package de.tobias.playpad.design.modern.serializer;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import org.dom4j.Element;

import java.util.UUID;

public class ModernCartDesignSerializer {

	public ModernCartDesign load(Element rootElement, Pad pad) {
		ModernCartDesign design;
		String uuidValue = rootElement.attributeValue("id");
		if (uuidValue != null) {
			design = new ModernCartDesign(pad, UUID.fromString(uuidValue));
		} else {
			design = new ModernCartDesign(pad);
		}

		Element backgroundElement = rootElement.element("BackgroundColor");
		if (backgroundElement != null) {
			try {
				design.setBackgroundColor(ModernColor.valueOf(backgroundElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			}
		}

		Element playElement = rootElement.element("PlayColor");
		if (playElement != null) {
			try {
				design.setPlayColor(ModernColor.valueOf(playElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			}
		}
		Element cueInElement = rootElement.element("CueInColor");
		if (cueInElement != null) {
			try {
				design.setCueInColor(ModernColor.valueOf(cueInElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				Logger.error(e);
			}
		}
		return design;
	}

	public void save(Element rootElement, ModernCartDesign design) {
		rootElement.addAttribute("id", design.getId().toString());
		rootElement.addElement("BackgroundColor").addText(design.getBackgroundColor().name());
		rootElement.addElement("PlayColor").addText(design.getPlayColor().name());
		rootElement.addElement("CueInColor").addText(design.getCueInColor().name());
	}
}
