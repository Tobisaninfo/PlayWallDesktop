package de.tobias.playpad.plugin.media.action.settings;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.ActionSettingsMappable;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;

public class BlackActionTypeSettingsEntry implements ActionSettingsEntry, ActionSettingsMappable {

	private Action action;

	public BlackActionTypeSettingsEntry(Action action) {
		this.action = action;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public String getName() {
		return Localization.getString("plugin.media.black_action.name");
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
