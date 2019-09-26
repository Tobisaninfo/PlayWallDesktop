package de.tobias.playpad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.viewcontroller.option.pad.trigger.CartTriggerViewController;

public class CartTriggerItemFactory extends TriggerItemFactory {

	public CartTriggerItemFactory(String type) {
		super(type);
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new CartTriggerItem(getType());
	}

	@Override
	public NVC getSettingsController(TriggerItem item) {
		return new CartTriggerViewController((CartTriggerItem) item);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.TRIGGER_CART_NAME);
	}
}
