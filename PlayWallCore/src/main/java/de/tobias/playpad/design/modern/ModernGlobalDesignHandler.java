package de.tobias.playpad.design.modern;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.stage.Stage;
import javafx.util.Duration;

public interface ModernGlobalDesignHandler {

	void applyCss(ModernGlobalDesign2 design, Stage stage);

	void applyCssMainView(ModernGlobalDesign2 design, IMainViewController controller, Stage stage, Project project);

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void handleWarning(ModernGlobalDesign2 design, IPadViewController controller, Duration warning);

	default void stopWarning(ModernGlobalDesign2 design, IPadViewController controller) {
	}
}
