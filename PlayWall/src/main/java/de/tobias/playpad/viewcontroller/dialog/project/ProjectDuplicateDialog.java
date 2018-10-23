package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class ProjectDuplicateDialog extends TextInputDialog {

	private ProjectReference ref;

	public ProjectDuplicateDialog(NVC parent, ProjectReference cloneableProject) {
		super(cloneableProject.getName());

		initOwner(parent.getContainingWindow());
		initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		Button button = (Button) getDialogPane().lookupButton(ButtonType.OK);
		button.setDisable(true);
		getEditor().textProperty().addListener((a, b, c) ->
		{
			if (!ProjectReferenceManager.validateProjectName(c)) {
				button.setDisable(true);
			} else {
				button.setDisable(false);
			}
		});

		setContentText(Localization.getString(Strings.UI_Dialog_NewProject_Content));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				ref = ProjectReferenceManager.duplicate(cloneableProject, name);
			} catch (Exception e) {
				e.printStackTrace();
				parent.showErrorMessage(Localization.getString(Strings.Error_Project_Save, name, e.getMessage()));
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
