package de.tobias.playpad.project;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.Element;

import de.tobias.utils.settings.Storable;

public class ProjectSettings {

	public static final int MAX_PAGES = 8;
	public static final int MAX_COLUMNS = 10;
	public static final int MAX_ROWS = 10;

	@Storable private int columns = 6;
	@Storable private int rows = 5;

	private boolean useMediaPath = false;
	private Path mediaPath = null;

	/**
	 * Returns the value of colums (Number of cells form left to right)
	 * 
	 * @return columns
	 */
	public int getColumns() {
		return columns;
	}

	/**
	 * Returns the value of rows (Number of cells from top to bottom
	 * 
	 * @return rows
	 */
	public int getRows() {
		return rows;
	}

	public Path getMediaPath() {
		return mediaPath;
	}

	public boolean isUseMediaPath() {
		return useMediaPath;
	}

	public void setColumns(int columns) {
		if (columns > MAX_COLUMNS)
			columns = MAX_COLUMNS;
		this.columns = columns;
	}

	public void setRows(int rows) {
		if (rows > MAX_ROWS)
			rows = MAX_ROWS;
		this.rows = rows;
	}

	public void setMediaPath(Path mediaPath) {
		this.mediaPath = mediaPath;
	}

	public void setUseMediaPath(boolean useMediaPath) {
		this.useMediaPath = useMediaPath;
	}

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

	public void save(Element element) {
		element.addElement(COLUMNS_ELEMENT).addText(String.valueOf(columns));
		element.addElement(ROWS_ELEMENT).addText(String.valueOf(rows));

		Element mediaPath = element.addElement(MEDIA_PATH_ELEMENT);
		if (this.mediaPath != null)
			mediaPath.addText(this.mediaPath.toString());
		mediaPath.addAttribute(MEDIA_PATH_ACTIVE_ATTR, String.valueOf(useMediaPath));
	}
}
