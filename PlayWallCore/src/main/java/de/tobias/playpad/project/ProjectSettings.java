package de.tobias.playpad.project;

import de.thecodelabs.storage.settings.annotation.Key;

import java.nio.file.Path;

public class ProjectSettings {

	public static final int MAX_PAGES = 10;

	public static final int MAX_COLUMNS = 15;
	public static final int MAX_ROWS = 15;

	public static final int MIN_COLUMNS = 3;
	public static final int MIN_ROWS = 1;

	@Key
	private int columns = 6;
	@Key
	private int rows = 5;

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

}
