package de.tobias.playpad.view.pad;

import de.tobias.playpad.project.page.PadIndex;

public class PadStyleClasses {

	private PadStyleClasses() {
	}

	public static final String STYLE_CLASS_PAD_BUTTON = "pad-button";
	public static final String STYLE_CLASS_PAD_BUTTON_INDEX = "pad${index}-button";

	public static final String STYLE_CLASS_PAD_ICON = "pad-icon";
	public static final String STYLE_CLASS_PAD_ICON_INDEX = "pad${index}-icon";

	public static final String STYLE_CLASS_PAD_INFO = "pad-info";
	public static final String STYLE_CLASS_PAD_INFO_INDEX = "pad${index}-info";

	public static final String STYLE_CLASS_PAD_TITLE = "pad-title";
	public static final String STYLE_CLASS_PAD_TITLE_INDEX = "pad${index}-title";

	public static String replaceIndex(String styleClass, PadIndex index) {
		return styleClass.replace("${index}", String.valueOf(index));
	}
}
