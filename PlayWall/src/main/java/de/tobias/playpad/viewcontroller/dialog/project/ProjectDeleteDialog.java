package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ref.ProjectReference;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Created by tobias on 26.02.17.
 */
class ProjectDeleteDialog extends Alert {

	ProjectDeleteDialog(ProjectReference reference, Window owner) {
		super(AlertType.CONFIRMATION);
		setHeaderText(Localization.getString(Strings.UI_Dialog_ProjectManager_Delete_Header));
		setContentText(Localization.getString(Strings.UI_Dialog_ProjectManager_Delete_Content, reference.getName()));

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);

		Stage stage = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);
	}
}
