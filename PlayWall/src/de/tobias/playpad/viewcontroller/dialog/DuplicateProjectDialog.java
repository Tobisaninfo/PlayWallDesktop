package de.tobias.playpad.viewcontroller.dialog;

import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DuplicateProjectDialog extends TextInputDialog {

	private ProjectReference ref;

	public DuplicateProjectDialog(ViewController controller, ProjectReference cloneableProject) {
		initOwner(controller.getStage());
		initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		setResultConverter(button ->
		{
			String param = getEditor().getText();
			if (!param.endsWith(PlayPadMain.projectType.substring(1))) {
				param += PlayPadMain.projectType.substring(1);
			}

			ButtonData data = button == null ? null : button.getButtonData();
			return data == ButtonData.OK_DONE ? param : null;
		});

		Button button = (Button) getDialogPane().lookupButton(ButtonType.OK);
		getEditor().textProperty().addListener((a, b, c) ->
		{
			if (ProjectReference.getProjects().contains(c) || !c.matches(Project.PROJECT_NAME_PATTERN)) {
				button.setDisable(true);
			} else {
				button.setDisable(false);
			}
		});

		setContentText(Localization.getString(Strings.UI_Dialog_NewProject_Content));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				if (ProfileReference.getProfiles().contains(name)) {
					controller.showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
					return;
				}

				ref = ProjectReference.duplicate(cloneableProject, name);
			} catch (Exception e) {
				e.printStackTrace();
				controller.showErrorMessage(Localization.getString(Strings.Error_Project_Save, name, e.getMessage()));
			}
		});
	}

	public Optional<ProjectReference> getName() {
		if (ref != null) {
			return Optional.of(ref);
		} else {
			return Optional.empty();
		}
	}
}
