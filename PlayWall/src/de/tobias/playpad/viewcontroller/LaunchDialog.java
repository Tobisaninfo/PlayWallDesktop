package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.*;
import de.tobias.playpad.project.importer.ConverterV6;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.playpad.viewcontroller.dialog.ModernPluginViewController;
import de.tobias.playpad.viewcontroller.dialog.NewProjectDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileChooseDialog;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static de.tobias.utils.util.Localization.getString;

public class LaunchDialog extends NVC implements ProjectReader.ProjectReaderDelegate {

	public static final String IMAGE = "icon.png";

	@FXML private Label infoLabel;
	@FXML private ImageView imageView;

	@FXML private ListView<ProjectReference> projectListView;

	@FXML private Button newProjectButton;
	@FXML private Button importProjectButton;
	@FXML private Button convertProjectButton;

	@FXML private Button openButton;
	@FXML private Button deleteButton;

	@FXML private Label cloudLabel;

	public LaunchDialog(Stage stage) {
		load("de/tobias/playpad/assets/dialog/", "launchDialog", PlayPadMain.getUiResourceBundle());
		setProjectListValues();

		applyViewControllerToStage(stage);
	}

	private void setProjectListValues() {
		projectListView.getItems().setAll(ProjectReferenceManager.getProjectsSorted());
	}

	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		// Setup launchscreen labels and image
		infoLabel.setText(getString(Strings.UI_Dialog_Launch_Info, app.getInfo().getName(), app.getInfo().getVersion()));
		imageView.setImage(new Image(IMAGE));

		openButton.setDisable(true);
		deleteButton.setDisable(true);

		// Load project to list
		projectListView.setPlaceholder(new Label(getString(Strings.UI_Placeholder_Project)));
		projectListView.setId("list");
		projectListView.setCellFactory(list -> new ProjectCell(true));

		// List selection listener
		projectListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			openButton.setDisable(c == null);
			deleteButton.setDisable(c == null);
		});

		// Mouse Double Click on list
		projectListView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					if (!projectListView.getSelectionModel().isEmpty()) {
						launchProject(getSelectedProject());
					}
				}
			}
		});

		// Cloud Info Label
		Server server = PlayPadPlugin.getServerHandler().getServer();
		FontIcon icon = new FontIcon(FontAwesomeType.CLOUD);
		switch (server.getConnectionState()) {
			case CONNECTED:
				icon.setColor(Color.BLACK);
				cloudLabel.setText(Localization.getString(Strings.Server_Connected));
				break;
			case CONNECTION_LOST:
				icon.setColor(Color.GRAY);
				cloudLabel.setText(Localization.getString(Strings.Server_Disconnected));
				break;
		}
		cloudLabel.setGraphic(icon);
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
		stage.centerOnScreen();
		stage.show();
	}

	@FXML
	private void newProjectButtonHandler(ActionEvent event) {
		NewProjectDialog dialog = new NewProjectDialog(getContainingWindow());
		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);

		ProjectReference projectRef = dialog.getProject();
		try {
			Project project = ProjectReferenceManager.loadProject(projectRef, this);
			PlayPadMain.getProgramInstance().openProject(project, e -> getStageContainer().ifPresent(NVCStage::close));
		} catch (DocumentException | IOException | ProjectNotFoundException | ProfileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void importProjectButtonHandler(ActionEvent event) {
		// TODO Import Projects
		/*FileChooser chooser = new FileChooser();
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
		}*/
	}

	@FXML
	private void convertProjectButtonHandler(ActionEvent event) {
		try {
			List<ProjectReference> projects = ConverterV6.loadProjectReferences();
			ChoiceDialog<ProjectReference> dialog = new ChoiceDialog<>(null, projects);

			dialog.setHeaderText(Localization.getString(Strings.UI_Dialog_Project_Convert_Header));
			dialog.setContentText(Localization.getString(Strings.UI_Dialog_Project_Convert_Content));

			dialog.initOwner(getContainingWindow());
			dialog.initModality(Modality.WINDOW_MODAL);
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

			Optional<ProjectReference> result = dialog.showAndWait();
			result.ifPresent((ref) -> {
				try {
					ConverterV6.convert(ref.getUuid(), ref.getName());
					ProjectReferenceManager.addProjectReference(ref);
					setProjectListValues();
				} catch (IOException | DocumentException e) {
					e.printStackTrace();
					showErrorMessage(Localization.getString(Strings.Error_Project_Convert));
				}
			});
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Standard_Gen));
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
					ProjectReferenceManager.removeProject(ref);
					projectListView.getItems().remove(ref); // VIEW
				} catch (IOException e) {
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
			Project project = ProjectReferenceManager.loadProject(ref, this);
			PlayPadMain.getProgramInstance().openProject(project, e -> getStageContainer().ifPresent(NVCStage::close));
		} catch (ProfileNotFoundException e) {
			e.printStackTrace();
			showErrorMessage(getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

			// Choose new profile
			ProfileReference profile = getProfileReference();
			ref.setProfileReference(profile);
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
	public ProfileReference getProfileReference() {
		ProfileChooseDialog dialog = new ProfileChooseDialog(getContainingWindow());

		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);
		Profile profile = dialog.getProfile();
		if (profile != null) {
			return profile.getRef();
		}
		return null;
	}
}
