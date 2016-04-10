package de.tobias.playpad.layout;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;

public abstract class LayoutConnect implements Displayable {

	public abstract String getType();

	public abstract CartLayout newCartLayout();

	public abstract GlobalLayout newGlobalLayout();

	public abstract CartLayoutViewController getCartLayoutViewController(CartLayout cartLayout);

	public abstract GlobalLayoutViewController getGlobalLayoutViewController(GlobalLayout globalLayout);
}
