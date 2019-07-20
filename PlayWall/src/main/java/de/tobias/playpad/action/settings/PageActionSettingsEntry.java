package de.tobias.playpad.action.settings;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.actions.PageAction;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;

public class PageActionSettingsEntry implements ActionSettingsEntry, ActionSettingsMappable {

	private Action action;

	public PageActionSettingsEntry(Action action) {
		this.action = action;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.Action_Navigate_toString, PageAction.getPageForAction(action));
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
