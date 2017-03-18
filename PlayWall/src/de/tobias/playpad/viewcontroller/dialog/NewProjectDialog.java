package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
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
public class NewProjectDialog extends NVC {

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

	public NewProjectDialog(Window owner) {
		load("de/tobias/playpad/assets/dialog/", "newProjectDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		profileComboBox.getItems().addAll(ProfileReferenceManager.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) -> finishButton.setDisable(validateNameInput(c)));
		finishButton.setDisable(true);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_NewProject_Title));
		stage.setWidth(560);
		stage.setHeight(380);

		stage.setMinWidth(560);
		stage.setMinHeight(380);

		stage.setMaxWidth(560);

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(stage);
		}
	}

	public Optional<ProjectReference> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
		return Optional.ofNullable(project);
	}

	@FXML
	private void finishButtonHandler(ActionEvent evenet) {
		try {
			ProfileReference profileReference = profileComboBox.getSelectionModel().getSelectedItem();

			String name = nameTextField.getText();
			boolean sync = syncCheckbox.isSelected();

			project = ProjectReferenceManager.addProject(name, profileReference, sync);

			getStageContainer().ifPresent(NVCStage::close);
		} catch (IOException e) {
			showErrorMessage(Localization.getString(Strings.Error_Project_Create, e.getLocalizedMessage()));
			e.printStackTrace();
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

	/**
	 * Validate the name input for a project name
	 *
	 * @param name project name to test
	 * @return <code>true</code> valid
	 */
	private boolean validateNameInput(String name) {
		return !name.isEmpty() && !(ProjectReferenceManager.getProjects().contains(name) || !name.matches(Project.PROJECT_NAME_PATTERN));
	}
}
