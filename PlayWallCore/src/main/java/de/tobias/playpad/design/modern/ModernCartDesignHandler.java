package de.tobias.playpad.design.modern;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.util.Duration;

public interface ModernCartDesignHandler {

	String convertToCss(ModernCartDesign2 design, String classSuffix, boolean fullCss, boolean flat);

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void handleWarning(ModernCartDesign2 design, IPadViewController controller, Duration warning, ModernGlobalDesign2 globalDesign);

	default void stopWarning(ModernCartDesign2 design, IPadViewController controller) {
	}

}
