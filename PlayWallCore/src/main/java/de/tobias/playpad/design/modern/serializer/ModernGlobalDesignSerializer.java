package de.tobias.playpad.design.modern.serializer;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import org.dom4j.Element;

public class ModernGlobalDesignSerializer {

	public ModernGlobalDesign load(Element rootElement) {
		ModernGlobalDesign design = new ModernGlobalDesign();
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

		Element animationElement = rootElement.element("Animation");
		if (animationElement != null) {
			Element warnAnimationElement = animationElement.element("Warn");
			if (warnAnimationElement != null) {
				design.setWarnAnimation(Boolean.valueOf(warnAnimationElement.getStringValue()));
			}
		}

		Element infoFontSizeElement = rootElement.element("InfoFontSize");
		if (infoFontSizeElement != null) {
			try {
				design.setInfoFontSize(Integer.valueOf(infoFontSizeElement.getStringValue()));
			} catch (NumberFormatException e) {
				Logger.error(e);
			}
		}

		Element titleFontSizeElement = rootElement.element("TitleFontSize");
		if (titleFontSizeElement != null) {
			try {
				design.setTitleFontSize(Integer.valueOf(titleFontSizeElement.getStringValue()));
			} catch (NumberFormatException e) {
				Logger.error(e);
			}
		}
		Element flatDesignElement = rootElement.element("FlatDesign");
		if (flatDesignElement != null) {
			design.setFlatDesign(Boolean.valueOf(flatDesignElement.getStringValue()));
		}

		return design;
	}

	public void save(Element rootElement, ModernGlobalDesign design) {
		rootElement.addElement("BackgroundColor").addText(design.getBackgroundColor().name());
		rootElement.addElement("PlayColor").addText(design.getPlayColor().name());
		Element animationElement = rootElement.addElement("Animation");
		animationElement.addElement("Warn").addText(String.valueOf(design.isWarnAnimation()));
		rootElement.addElement("InfoFontSize").addText(String.valueOf(design.getInfoFontSize()));
		rootElement.addElement("TitleFontSize").addText(String.valueOf(design.getTitleFontSize()));
		rootElement.addElement("FlatDesign").addText(String.valueOf(design.isFlatDesign()));
	}
}
