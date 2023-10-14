package de.tobias.playpad.tigger;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public abstract class TriggerItemFactory extends Component {

	public TriggerItemFactory(String type) {
		super(type);
	}

	public abstract TriggerItem newInstance(Trigger trigger);

	public abstract NVC getSettingsController(TriggerItem item, IMainViewController mainViewController);
}
