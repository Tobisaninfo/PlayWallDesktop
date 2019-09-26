package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.viewcontroller.dialog.profile.NewProfileDialog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;

/**
 * Create an new project and adds it to the project reference list
 *
 * @author tobias
 */
public class ProjectNewDialog extends NVC {

	@FXML
	private TextField nameTextField;
	@FXML
	private CheckBox syncCheckbox;
	@FXML
	private ComboBox<ProfileReference> profileComboBox;
	@FXML
	private Button newProfileButton;

	@FXML
	private Button finishButton;
	@FXML
	private Button cancelButton;

	private ProjectReference project;

	public ProjectNewDialog(Window owner) {
		load("view/dialog", "NewProjectDialog", Localization.getBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		profileComboBox.getItems().addAll(ProfileReferenceManager.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) -> finishButton.setDisable(!ProjectReferenceManager.validateProjectName(c)));
		finishButton.setDisable(true);
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
		PlayPadPlugin.styleable().applyStyle(stage);
		stage.initModality(Modality.WINDOW_MODAL);

		stage.setTitle(Localization.getString(Strings.UI_DIALOG_NEW_PROJECT_TITLE));
		stage.setWidth(560);
		stage.setHeight(380);

		stage.setMinWidth(560);
		stage.setMinHeight(380);

		stage.setMaxWidth(560);
	}

	public Optional<ProjectReference> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
		return Optional.ofNullable(project);
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		try {
			ProfileReference profileReference = profileComboBox.getSelectionModel().getSelectedItem();

			String name = nameTextField.getText();
			boolean sync = syncCheckbox.isSelected();

			project = ProjectReferenceManager.addProject(name, profileReference, sync);

			getStageContainer().ifPresent(NVCStage::close);
		} catch (IOException e) {
			showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_CREATE, e.getLocalizedMessage()));
			Logger.error(e);
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProfileDialog dialog = new NewProfileDialog(getContainingWindow());
		dialog.showAndWait().ifPresent(profile -> {
			// Add new Profile to combo box and select it
			profileComboBox.getItems().add(profile.getRef());
			profileComboBox.getSelectionModel().selectLast();
		});
	}
}
