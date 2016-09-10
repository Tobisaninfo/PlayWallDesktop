package de.tobias.playpad.viewcontroller;

import static de.tobias.utils.util.Localization.getString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProfileChooseable;
import de.tobias.playpad.project.ProjectImporter;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.dialog.NewProjectDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileChooseDialog;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.ViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LaunchDialog extends ViewController implements ProfileChooseable {

	private static final String IMAGE = "icon.png";

	@FXML private Label infoLabel;
	@FXML private ImageView imageView;

	@FXML private ListView<ProjectReference> projectListView;

	@FXML private Button newProfileButton;
	@FXML private Button importProfileButton;
	@FXML private Button openButton;
	@FXML private Button deleteButton;

	public LaunchDialog(Stage stage) {
		super("launchDialog", "de/tobias/playpad/assets/dialog/", stage, null, PlayPadMain.getUiResourceBundle());
		projectListView.getItems().addAll(ProjectReference.getProjectsSorted());
	}

	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_Dialog_Launch_Info, app.getInfo().getName(), app.getInfo().getVersion()));
		try {
			imageView.setImage(new Image(IMAGE));
		} catch (Exception e) {
			e.printStackTrace();
		}

		openButton.setDisable(true);
		deleteButton.setDisable(true);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_Placeholder_Project)));
		projectListView.setId("list");
		projectListView.setCellFactory(list -> new ProjectCell(true));

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on list
		projectListView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					if (!projectListView.getSelectionModel().isEmpty()) {
						launchProject(getSelectedProject());
					}
				}
			}
		});
	}

	@Override
	public void initStage(Stage stage) {
		setCSS("style.css", "de/tobias/playpad/assets/");
		setCSS("launchDialog_style.css", "de/tobias/playpad/assets/style/");

		stage.setTitle(getString(Strings.UI_Dialog_Launch_Title));
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);
		
		stage.setResizable(false);
		stage.setWidth(650);
		stage.show();
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProjectDialog dialog = new NewProjectDialog(getStage());
		dialog.getStage().showAndWait();

		Project project = dialog.getProject();
		if (project != null) {
			PlayPadMain.getProgramInstance().openProject(project);
			getStage().close();
		}
	}

	@FXML
	private void importProfileButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter(getString(Strings.File_Filter_ZIP), PlayPadMain.projectZIPType));
		File file = chooser.showOpenDialog(getStage());
		if (file != null) {
			Path zipFile = file.toPath();
			try {
				ImportDialog importDialog = ImportDialog.getInstance(getStage());
				ProjectReference ref = ProjectImporter.importProject(zipFile, importDialog, importDialog);
				if (ref != null) {
					launchProject(ref);
				}
			} catch (DocumentException | IOException e) {
				showErrorMessage(getString(Strings.Error_Project_Open, e.getLocalizedMessage()));
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void openButtonHandler(ActionEvent event) {
		launchProject(getSelectedProject());
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		if (getSelectedProject() != null) {
			ProjectReference ref = getSelectedProject();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText(getString(Strings.UI_Dialog_ProjectManager_Delete_Content, ref));

			Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
			PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);
			alert.initOwner(getStage());
			alert.initModality(Modality.WINDOW_MODAL);

			alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
			{
				try {
					ProjectReference.removeDocument(ref);
					projectListView.getItems().remove(ref); // VIEW
				} catch (DocumentException | IOException e) {
					showErrorMessage(getString(Strings.Error_Project_Delete, e.getLocalizedMessage()));
				}
			});
		}
	}

	/**
	 * Gibt das ausgewählte Projekt zurück.
	 * 
	 * @return Projekt
	 */
	private ProjectReference getSelectedProject() {
		return projectListView.getSelectionModel().getSelectedItem();
	}

	/**
	 * Launch a project and close this view.
	 * 
	 * @param ref
	 *            Project to launch
	 */
	private void launchProject(ProjectReference ref) {
		try {
			Project project = Project.load(ref, true, this);
			PlayPadMain.getProgramInstance().openProject(project);
			getStage().close();
		} catch (ProfileNotFoundException e) {
			e.printStackTrace();
			showErrorMessage(getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

			// Neues Profile wählen
			Profile profile = getUnkownProfile();
			ref.setProfileReference(profile.getRef());
		} catch (ProjectNotFoundException e) {
			e.printStackTrace();
			showErrorMessage(getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
		}
	}

	// Zeigt dialog für das Ausfählen eines neuen Profiles.
	@Override
	public Profile getUnkownProfile() {
		ProfileChooseDialog dialog = new ProfileChooseDialog(getStage());

		dialog.getStage().showAndWait();
		Profile profile = dialog.getProfile();
		if (profile != null) {
			return profile;
		}
		return null;
	}
}
