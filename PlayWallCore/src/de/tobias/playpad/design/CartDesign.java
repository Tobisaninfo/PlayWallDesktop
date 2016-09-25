package de.tobias.playpad.design;

import org.dom4j.Element;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.util.Duration;

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

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	public abstract void handleWarning(IPadViewController controller, Duration warning, GlobalDesign animate);

	public default void stopWarning(IPadViewController controller) {}

	public void reset();

	public void copyGlobalLayout(GlobalDesign globalLayout);
	
	Object clone() throws CloneNotSupportedException;
}
