package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import javafx.util.Duration;

public interface ModernCartDesignHandler {

	String generateCss(ModernCartDesign design, String classSuffix, boolean flat);

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void handleWarning(ModernCartDesign design, AbstractPadViewController controller, Duration warning, ModernGlobalDesign globalDesign);

	default void stopWarning(ModernCartDesign design, AbstractPadViewController controller) {
	}

}
