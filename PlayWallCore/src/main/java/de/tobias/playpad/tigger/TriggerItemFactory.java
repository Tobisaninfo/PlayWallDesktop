package de.tobias.playpad.tigger;

import de.tobias.playpad.registry.Component;
import de.tobias.utils.ui.NVC;

public abstract class TriggerItemFactory extends Component {

	public TriggerItemFactory(String type) {
		super(type);
	}

	public abstract TriggerItem newInstance(Trigger trigger);

	public abstract NVC getSettingsController(TriggerItem item);

}
