package de.tobias.playpad.action.settings;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;

public class PageActionTypeSettingsEntry implements ActionSettingsEntry {
	@Override
	public String getName() {
		return Localization.getString(Strings.ACTION_PAGE_NAME);
	}

	@Override
	public FontIcon getIcon() {
		return null;
	}

	@Override
	public NVC getDetailSettingsController(Mapping mapping, IMappingTabViewController controller) {
		return null;
	}
}
