package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.*;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.dialog.ModernPluginViewController;
import de.tobias.playpad.viewcontroller.dialog.NewProjectDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileChooseDialog;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static de.tobias.utils.util.Localization.getString;

public class LaunchDialog extends NVC implements ProfileChooseable {

	private static final String IMAGE = "icon.png";

	@FXML private Label infoLabel;
	@FXML private ImageView imageView;

	@FXML private ListView<ProjectReference> projectListView;

	@FXML private Button newProfileButton;
	@FXML private Button importProfileButton;
	@FXML private Button openButton;
	@FXML private Button deleteButton;

	public LaunchDialog(Stage stage) {
		load("de/tobias/playpad/assets/dialog/", "launchDialog", PlayPadMain.getUiResourceBundle());
		projectListView.getItems().addAll(ProjectReferences.getProjectsSorted());

		applyViewControllerToStage(stage);
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
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/style.css");
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/style/launchDialog_style.css");

		stage.setTitle(getString(Strings.UI_Dialog_Launch_Title));
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setResizable(false);
		stage.setWidth(650);
		stage.setHeight(400);
		stage.show();
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProjectDialog dialog = new NewProjectDialog(getContainingWindow());
		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);

		Project project = dialog.getProject();
		if (project != null) {
			PlayPadMain.getProgramInstance().openProject(project, e -> getStageContainer().ifPresent(NVCStage::close));
		}
	}

	@FXML
	private void importProfileButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter(getString(Strings.File_Filter_ZIP), PlayPadMain.projectZIPType));
		File file = chooser.showOpenDialog(getContainingWindow());
		if (file != null) {
			Path zipFile = file.toPath();
			try {
				ImportDialog importDialog = ImportDialog.getInstance(getContainingWindow());
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
			alert.initOwner(getContainingWindow());
			alert.initModality(Modality.WINDOW_MODAL);

			alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
			{
				try {
					ProjectReferences.removeDocument(ref);
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
	 * Öffnet ein Project und zeigt es im MainView an. Zudem wird as entsprechende Profile geladen und geprüft ob Module (Plugins) fehlen.
	 *
	 * @param ref Project to launch
	 */
	private void launchProject(ProjectReference ref) {
		// Es fehlen Module
		if (!ref.getMissedModules().isEmpty()) {
			showInfoMessage(Localization.getString(Strings.Error_Plugins_Missing));

			ModernPluginViewController pluginViewController = new ModernPluginViewController(getContainingWindow(), ref.getMissedModules());
			pluginViewController.getStageContainer().ifPresent(NVCStage::showAndWait);
		}

		try {
			Project project = ProjectSerializer.load(ref, true, this);
			PlayPadMain.getProgramInstance().openProject(project, e -> getStageContainer().ifPresent(NVCStage::close));
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
		ProfileChooseDialog dialog = new ProfileChooseDialog(getContainingWindow());

		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);
		Profile profile = dialog.getProfile();
		if (profile != null) {
			return profile;
		}
		return null;
	}
}
