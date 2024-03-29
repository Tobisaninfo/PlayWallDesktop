package de.tobias.playpad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.pad.trigger.VolumeTriggerViewController;

public class VolumeTriggerItemFactory extends TriggerItemFactory {

	public VolumeTriggerItemFactory(String type) {
		super(type);
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new VolumeTriggerItem(getType());
	}

	@Override
	public NVC getSettingsController(TriggerItem item, IMainViewController mainViewController) {
		return new VolumeTriggerViewController((VolumeTriggerItem) item, mainViewController);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.TRIGGER_VOLUME_NAME);
	}
}
