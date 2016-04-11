package de.tobias.playpad.viewcontroller.dialog;

import java.io.IOException;
import java.util.UUID;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Create an new project and adds it to the project reference list
 * 
 * @author tobias
 *
 */
public class NewProjectDialog extends ViewController {

	@FXML private TextField nameTextField;
	@FXML private ComboBox<ProfileReference> profileComboBox;
	@FXML private Button newProfileButton;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	private Project project;

	public NewProjectDialog(Window owner) {
		super("newProjectDialog", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);

		profileComboBox.getItems().addAll(ProfileReference.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.isEmpty()) {
				finishButton.setDisable(true);
			} else {
				if (ProjectReference.getProjects().contains(c) || !c.matches(Project.projectNameEx)) {
					finishButton.setDisable(true);
					return;
				}
				finishButton.setDisable(false);
			}
		});
		finishButton.setDisable(true);

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_NewProject_Title));
		stage.setWidth(560);
		stage.setHeight(250);

		stage.setMinWidth(560);
		stage.setMinHeight(250);

		stage.setMaxWidth(560);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(getStage());
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent evenet) {
		try {
			Profile profile = Profile.load(profileComboBox.getSelectionModel().getSelectedItem());

			String name = nameTextField.getText();
			UUID uuid = UUID.randomUUID();

			ProjectReference projectReference = new ProjectReference(uuid, name, profile.getRef());

			project = new Project(projectReference);
			project.save();

			ProjectReference.addProject(projectReference);

			getStage().close();
		} catch (IOException | DocumentException | ProfileNotFoundException e) {
			showErrorMessage(Localization.getString(Strings.Error_Project_Create, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProfileDialog dialog = new NewProfileDialog(getStage());
		dialog.getStage().showAndWait();

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
