package de.tobias.playpad.viewcontroller.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferences;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Create an new project and adds it to the project reference list
 * 
 * @author tobias
 *
 */
public class NewProjectDialog extends NVC {

	@FXML private TextField nameTextField;
	@FXML private ComboBox<ProfileReference> profileComboBox;
	@FXML private Button newProfileButton;

	// Media Path
	@FXML private CheckBox mediaPathCheckbox;
	@FXML private Button mediaButtonChoose;
	@FXML private Label mediaPathLabel;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	private Project project;
	private Path newMediaPath; // Ausgewählter Ordner (temp)

	public NewProjectDialog(Window owner) {
		load("de/tobias/playpad/assets/dialog/", "newProjectDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		profileComboBox.getItems().addAll(ProfileReferences.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.isEmpty()) {
				finishButton.setDisable(true);
			} else {
				if (ProjectReferences.getProjects().contains(c) || !c.matches(Project.PROJECT_NAME_PATTERN)) {
					finishButton.setDisable(true);
					return;
				}
				finishButton.setDisable(false);
			}
		});
		finishButton.setDisable(true);

		mediaPathCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			mediaButtonChoose.setDisable(!c);
			if (!c) {
				mediaPathLabel.setText("");
				newMediaPath = null;
			}
		});
		mediaButtonChoose.setDisable(true);
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

	@FXML
	private void mediaButtonHandler(ActionEvent event) {
		if (mediaPathCheckbox.isSelected()) {
			DirectoryChooser chooser = new DirectoryChooser();
			File file = chooser.showDialog(getContainingWindow());
			if (file != null) {
				newMediaPath = file.toPath();
				mediaPathLabel.setText(newMediaPath.toString());
			}
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent evenet) {
		if (mediaPathCheckbox.isSelected() && newMediaPath == null) {
			showInfoMessage(Localization.getString(Strings.UI_Dialog_NewProject_Content));
			return;
		}

		try {
			Profile profile = Profile.load(profileComboBox.getSelectionModel().getSelectedItem());
			String name = nameTextField.getText();
			UUID uuid = UUID.randomUUID();

			ProjectReference projectReference = new ProjectReference(uuid, name, profile.getRef());
			project = new Project(projectReference);
			project.getSettings().setUseMediaPath(mediaPathCheckbox.isSelected());
			project.getSettings().setMediaPath(newMediaPath);
			project.save();

			ProjectReferences.addProject(projectReference);

			getStageContainer().ifPresent(NVCStage::close);
		} catch (IOException | DocumentException | ProfileNotFoundException e) {
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
		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);

		Profile profile = dialog.getProfile();

		// In GUI hinzufügen (am Ende) und auswählen
		if (profile != null) {
			profileComboBox.getItems().add(profile.getRef());
			profileComboBox.getSelectionModel().selectLast();
		}
	}

	public Project getProject() {
		return project;
	}

}
