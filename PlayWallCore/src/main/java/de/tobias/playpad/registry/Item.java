package de.tobias.playpad.registry;

import de.thecodelabs.utils.ui.icon.FontIconType;
import de.tobias.playpad.plugin.Module;

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
