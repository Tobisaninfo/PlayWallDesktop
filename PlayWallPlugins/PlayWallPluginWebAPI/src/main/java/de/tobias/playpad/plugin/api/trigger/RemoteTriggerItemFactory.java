package de.tobias.playpad.plugin.api.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class RemoteTriggerItemFactory extends TriggerItemFactory {

	public RemoteTriggerItemFactory(String type) {
		super(type);
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new RemoteTriggerItem(getType());
	}

	@Override
	public NVC getSettingsController(TriggerItem item, IMainViewController mainViewController) {
		return new RemoteTriggerItemSettingsController((RemoteTriggerItem) item);
	}
}
