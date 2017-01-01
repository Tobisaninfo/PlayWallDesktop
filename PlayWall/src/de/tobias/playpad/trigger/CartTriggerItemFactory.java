package de.tobias.playpad.trigger;

import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.viewcontroller.option.pad.trigger.CartTriggerViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontIconType;
import de.tobias.utils.util.Localization;

public class CartTriggerItemFactory extends TriggerItemFactory {

	public CartTriggerItemFactory(String type) {
		super(type);
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new CartTriggerItem(getType());
	}

	@Override
	public ContentViewController getSettingsController(TriggerItem item) {
		return new CartTriggerViewController((CartTriggerItem) item);
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Trigger_Cart_Name);
	}
}