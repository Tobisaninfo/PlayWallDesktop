package de.tobias.playpad.project;

import org.dom4j.Element;

import java.nio.file.Paths;

/**
 * Created by tobias on 19.02.17.
 */
class ProjectSettingsSerializer {

	private static final String ROWS_ELEMENT = "Rows";
	private static final String COLUMNS_ELEMENT = "Columns";

	private static final String MEDIA_PATH_ELEMENT = "MediaPath";
	private static final String MEDIA_PATH_ACTIVE_ATTR = "active";

	public static ProjectSettings load(Element element) {
		ProjectSettings settings = new ProjectSettings();
		if (element.element(COLUMNS_ELEMENT) != null)
			settings.setColumns(Integer.valueOf(element.element(COLUMNS_ELEMENT).getStringValue()));
		if (element.element(ROWS_ELEMENT) != null)
			settings.setRows(Integer.valueOf(element.element(ROWS_ELEMENT).getStringValue()));

		Element mediaElement = element.element(MEDIA_PATH_ELEMENT);
		if (mediaElement != null) {
			settings.setMediaPath(Paths.get(mediaElement.getStringValue()));
			settings.setUseMediaPath(Boolean.valueOf(mediaElement.attributeValue(MEDIA_PATH_ACTIVE_ATTR)));
		}

		return settings;
	}

	public static void save(ProjectSettings settings, Element element) {
		element.addElement(COLUMNS_ELEMENT).addText(String.valueOf(settings.getColumns()));
		element.addElement(ROWS_ELEMENT).addText(String.valueOf(settings.getRows()));

		Element mediaPath = element.addElement(MEDIA_PATH_ELEMENT);
		if (settings.isUseMediaPath())
			mediaPath.addText(settings.getMediaPath().toString());
		mediaPath.addAttribute(MEDIA_PATH_ACTIVE_ATTR, String.valueOf(settings.isUseMediaPath()));
	}
}
