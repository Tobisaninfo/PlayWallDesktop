package de.tobias.playpad.server.sync;

/**
 * Created by tobias on 19.02.17.
 */
public class PropertyDef {

	private PropertyDef() {
	}

	public static final String ID = "id";
	public static final String CMD = "cmd";
	public static final String TIME = "time";
	public static final String PROJECT_REF = "project";

	public static final String FIELD = "field";
	public static final String VALUE = "value";

	public static final String PROJECT_NAME = "name";

	public static final String PAGE_POSITION = "position";
	public static final String PAGE_NAME = "name";
	public static final String PAGE_PROJECT_REF = "project";

	public static final String PAD_POSITION = "position";
	public static final String PAD_NAME = "name";
	public static final String PAD_PAGE_REF = "page";
	public static final String PAD_CONTENT_TYPE = "contentType";

	public static final String PATH_FILENAME = "filename";
	public static final String PATH_PAD_REF = "pad";

	public static final String DESIGN_BACKGROUND_COLOR = "background_color";
	public static final String DESIGN_PLAY_COLOR = "play_color";
	public static final String DESIGN_PAD_SETTINGS_REF = "pad_settings";

	public static final String PAD_SETTINGS_PAD_ID = "pad_id";
	public static final String PAD_SETTINGS_VOLUME = "volume";
	public static final String PAD_SETTINGS_LOOP = "loop";
	public static final String PAD_SETTINGS_TIME_MODE = "time_mode";
	public static final String PAD_SETTINGS_WARNING = "warning";
}
