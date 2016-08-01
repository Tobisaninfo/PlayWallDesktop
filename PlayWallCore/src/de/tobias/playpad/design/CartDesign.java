package de.tobias.playpad.design;

import org.dom4j.Element;

import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.settings.Warning;

public interface CartDesign {

	public String convertToCss(String classSufix, boolean fullCss);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	public void load(Element rootElement);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	public void save(Element rootElement);

	/**
	 * Wird in einem neuen Thread aufgerufen
	 * 
	 * @param controller
	 * @param warning
	 */
	public abstract void handleWarning(IPadViewControllerV2 controller, Warning warning, GlobalDesign animate);

	public default void stopWarning(IPadViewControllerV2 controller) {}

	public void reset();

	public void copyGlobalLayout(GlobalDesign globalLayout);
}
