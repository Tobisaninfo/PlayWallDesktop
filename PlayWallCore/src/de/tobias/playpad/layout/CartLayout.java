package de.tobias.playpad.layout;

import org.dom4j.Element;

import de.tobias.playpad.pad.Warning;
import de.tobias.playpad.pad.view.IPadViewController;

public interface CartLayout {

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
	public abstract void handleWarning(IPadViewController controller, Warning warning, GlobalLayout animate);

	public default void stopWarning(IPadViewController controller) {}

	public void reset();

	public void copyGlobalLayout(GlobalLayout globalLayout);
}
