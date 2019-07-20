package de.tobias.playpad.action.settings;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;

public interface ActionSettingsEntry {

	String getName();

	FontIcon getIcon();

	NVC getDetailSettingsController(Mapping mapping, IMappingTabViewController controller);
}
