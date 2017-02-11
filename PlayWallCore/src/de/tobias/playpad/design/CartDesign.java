package de.tobias.playpad.design;

import org.dom4j.Element;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.util.Duration;

public interface CartDesign {

	String convertToCss(String classSufix, boolean fullCss);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	void load(Element rootElement);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	void save(Element rootElement);

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void handleWarning(IPadViewController controller, Duration warning, GlobalDesign animate);

	default void stopWarning(IPadViewController controller) {}

	void reset();

	void copyGlobalLayout(GlobalDesign globalLayout);
	
	Object clone() throws CloneNotSupportedException;
}
