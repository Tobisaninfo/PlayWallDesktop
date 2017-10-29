package de.tobias.playpad.design.modern.serializer;

import de.tobias.playpad.design.modern.ModernCartDesign2;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.pad.Pad;
import org.dom4j.Element;

import java.util.UUID;

public class ModernCartDesignSerializer {

	public ModernCartDesign2 load(Element rootElement, Pad pad) {
		ModernCartDesign2 design;
		String uuidValue = rootElement.attributeValue("id");
		if (uuidValue != null) {
			design = new ModernCartDesign2(pad, UUID.fromString(uuidValue));
		} else {
			design = new ModernCartDesign2(pad);
		}

		Element backgroundElement = rootElement.element("BackgroundColor");
		if (backgroundElement != null) {
			try {
				design.setBackgroundColor(ModernColor.valueOf(backgroundElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		Element playElement = rootElement.element("PlayColor");
		if (playElement != null) {
			try {
				design.setPlayColor(ModernColor.valueOf(playElement.getStringValue()));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return design;
	}

	public void save(Element rootElement, ModernCartDesign2 design) {
		rootElement.addElement("BackgroundColor").addText(design.getBackgroundColor().name());
		rootElement.addElement("PlayColor").addText(design.getPlayColor().name());
	}
}
