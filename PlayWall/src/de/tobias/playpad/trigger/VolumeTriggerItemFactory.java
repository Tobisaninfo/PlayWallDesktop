package de.tobias.playpad.trigger;

import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.viewcontroller.option.pad.trigger.VolumeTriggerViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontIconType;
import de.tobias.utils.util.Localization;

public class VolumeTriggerItemFactory extends TriggerItemFactory {

	public VolumeTriggerItemFactory(String type) {
		super(type);
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new VolumeTriggerItem(getType());
	}

	@Override
	public ContentViewController getSettingsController(TriggerItem item) {
		return new VolumeTriggerViewController((VolumeTriggerItem) item);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Trigger_Volume_Name);
	}
}
