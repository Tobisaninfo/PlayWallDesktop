package de.tobias.playpad.server.sync.command;

/**
 * Created by tobias on 19.02.17.
 */
public class Commands {

	private Commands() {
	}

	public static final String PROJECT_ADD = "pro-add";
	public static final String PROJECT_UPDATE = "pro-update";
	public static final String PROJECT_REMOVE = "pro-rm";

	public static final String PAGE_ADD = "page-add";
	public static final String PAGE_UPDATE = "page-update";
	public static final String PAGE_REMOVE = "page-rm";

	public static final String PAD_ADD = "pad-add";
	public static final String PAD_UPDATE = "pad-update";
	public static final String PAD_REMOVE = "pad-rm";
	public static final String PAD_CLEAR = "pad-clear";
	public static final String PAD_MOVE = "pad-mv";

	public static final String PATH_ADD = "path-add";
	public static final String PATH_REMOVE = "path-rm";

	public static final String DESIGN_ADD = "design-add";
	public static final String DESIGN_UPDATE = "design-update";

	public static final String PAD_SETTINGS_ADD = "pad-settings-add"; // Called in Page when pad is added
	public static final String PAD_SETTINGS_UPDATE = "pad-settings-update";
}

