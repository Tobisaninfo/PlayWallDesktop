package de.tobias.playpad.viewcontroller.dialog.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class DuplicateProjectDialog extends TextInputDialog {

	private ProjectReference ref;

	public DuplicateProjectDialog(NVC controller, ProjectReference cloneableProject) {
		initOwner(controller.getContainingWindow());
		initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		Button button = (Button) getDialogPane().lookupButton(ButtonType.OK);
		getEditor().textProperty().addListener((a, b, c) ->
		{
			if (ProjectReferenceManager.getProjects().contains(c) || !c.matches(Project.PROJECT_NAME_PATTERN)) {
				button.setDisable(true);
			} else {
				button.setDisable(false);
			}
		});

		setContentText(Localization.getString(Strings.UI_Dialog_NewProject_Content));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				if (ProfileReferenceManager.getProfiles().contains(name)) {
					controller.showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
					return;
				}

				ref = ProjectReferenceManager.duplicate(cloneableProject, name);
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
