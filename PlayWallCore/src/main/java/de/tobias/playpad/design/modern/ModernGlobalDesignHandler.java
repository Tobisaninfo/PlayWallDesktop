package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.stage.Stage;

public interface ModernGlobalDesignHandler
{

	void applyStyleSheet(Stage stage);

	void applyStyleSheetToMainViewController(ModernGlobalDesign design, IMainViewController controller, Stage stage, Project project);
}
