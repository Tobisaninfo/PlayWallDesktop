package de.tobias.playpad.registry;

import de.tobias.playpad.plugin.Module;
import de.tobias.utils.ui.icon.FontIconType;

/**
 * Created by tobias on 31.12.16.
 */
class Item<C> {
	C content;
	Module module;
	String localozedName;

	// UI Description
	FontIconType fontIconType;
	int iconSize;
}
