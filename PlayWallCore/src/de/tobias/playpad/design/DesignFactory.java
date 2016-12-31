package de.tobias.playpad.design;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.utils.ui.icon.FontIconType;

public abstract class DesignFactory extends Component implements Displayable {

	public DesignFactory(String type) {
		super(type);
	}

	public abstract CartDesign newCartDesign();

	public abstract GlobalDesign newGlobalDesign();

	public abstract CartDesignViewController getCartDesignViewController(CartDesign cartLayout);

	public abstract GlobalDesignViewController getGlobalDesignViewController(GlobalDesign globalLayout);
}
