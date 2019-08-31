package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
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
		setHeaderText(Localization.getString(Strings.UI_DIALOG_PROJECT_MANAGER_DELETE_HEADER));
		setContentText(Localization.getString(Strings.UI_DIALOG_PROJECT_MANAGER_DELETE_CONTENT, reference.getName()));

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);

		Stage stage = (Stage) getDialogPane().getScene().getWindow();
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
	}
}
