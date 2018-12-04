package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
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
		load("view/dialog", "NewProjectDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		profileComboBox.getItems().addAll(ProfileReferenceManager.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) -> finishButton.setDisable(!Project.validateNameInput(c)));
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
			ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
			PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyStyleSheet(design, stage);
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
}
