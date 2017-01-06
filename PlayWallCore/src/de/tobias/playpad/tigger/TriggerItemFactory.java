package de.tobias.playpad.tigger;

import de.tobias.playpad.registry.Component;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontIconType;

public abstract class TriggerItemFactory extends Component {

	public TriggerItemFactory(String type) {
		super(type);
	}

	public abstract TriggerItem newInstance(Trigger trigger);

	public abstract NVC getSettingsController(TriggerItem item);

}
