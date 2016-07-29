package de.tobias.playpad.design;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;

public abstract class DesignConnect implements Displayable {

	public abstract String getType();

	public abstract CartDesign newCartDesign();

	public abstract GlobalDesign newGlobalDesign();

	public abstract CartDesignViewController getCartDesignViewController(CartDesign cartLayout);

	public abstract GlobalDesignViewController getGlobalDesignViewController(GlobalDesign globalLayout);
}
