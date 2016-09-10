package de.tobias.playpad.viewcontroller.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProjectImporter;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.TimeUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProjectManagerDialog extends ViewController implements NotificationHandler {

	@FXML private ListView<ProjectReference> projectList;

	@FXML private Button openButton;
	@FXML private Button cancelButton;

	@FXML private TextField nameTextField;
	@FXML private Button renameButton;

	@FXML private Button newButton;
	@FXML private Button duplicateButton;
	@FXML private Button deleteButton;

	@FXML private Button importButton;
	@FXML private Button exportButton;

	@FXML private Label dateLabel;
	@FXML private Label profileLabel;

	private Project currentProject;

	private NotificationPane notificationPane;
	@FXML private AnchorPane rootNode;

	private boolean cancel = false;

	public ProjectManagerDialog(Window owner, Project currentProject) {
		super("openDialog", "de/tobias/playpad/assets/dialog/project/", null, PlayPadMain.getUiResourceBundle());
		this.currentProject = currentProject;

		projectList.getItems().setAll(ProjectReference.getProjectsSorted());

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);
	}

	@Override
	public void init() {
		// Notification Handler
		notificationPane = new NotificationPane(rootNode);
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

		setAnchor(notificationPane, 0, 0, 0, 0);
		((AnchorPane) getParent()).getChildren().add(notificationPane);

		projectList.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Project)));
		projectList.setCellFactory(list -> new ProjectCell(false));

		projectList.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					if (!projectList.getSelectionModel().isEmpty()) {
						getStage().close();
					}
				}
			}
		});
		projectList.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			if (c != null) {
				ProjectReference ref = getSelectedProject();

				openButton.setDisable(false);
				duplicateButton.setDisable(false);
				exportButton.setDisable(false);
				renameButton.setDisable(false);

				nameTextField.setText(c.toString());
				try {
					ProfileReference profileRef = ref.getProfileReference();
					profileLabel.setText(profileRef.getName());
					dateLabel.setText(TimeUtils.getDfmLong().format(ref.getLastMofied()));
				} catch (Exception e) {
					profileLabel.setText("-");
					dateLabel.setText("-");
					e.printStackTrace();
				}

				if (currentProject.getProjectReference().equals(c)) {
					deleteButton.setDisable(true);
				} else {
					deleteButton.setDisable(false);
				}
			} else {
				openButton.setDisable(true);
				deleteButton.setDisable(true);
				duplicateButton.setDisable(true);
				exportButton.setDisable(true);
				renameButton.setDisable(true);

				nameTextField.setText(null);
				profileLabel.setText("-");
				dateLabel.setText("-");
			}
		});

		openButton.setDisable(true);
		deleteButton.setDisable(true);
		duplicateButton.setDisable(true);
		exportButton.setDisable(true);
		renameButton.setDisable(true);

		profileLabel.setText("-");
		dateLabel.setText("-");

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setWidth(600);
		stage.setHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_ProjectManager_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		ProjectReference ref = getSelectedProject();

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setContentText(Localization.getString(Strings.UI_Dialog_ProjectManager_Delete_Content, ref));

		Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);
		alert.initOwner(getStage());
		alert.initModality(Modality.WINDOW_MODAL);

		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
		{
			try {
				ProjectReference.removeDocument(ref);
				projectList.getItems().remove(ref); // VIEW
			} catch (Exception e) {
				showErrorMessage(Localization.getString(Strings.Error_Project_Delete, e.getLocalizedMessage()));
			}
		});
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		cancel = true;
		getStage().close();
	}

	@FXML
	private void openButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void renameButtonHandler(ActionEvent event) {
		ProjectReference projectReference = getSelectedProject();
		String oldName = projectReference.toString();

		try {
			String newProjectName = (String) nameTextField.getText();
			if (ProjectReference.getProjects().contains(newProjectName) || !nameTextField.getText().matches(Project.PROJECT_NAME_PATTERN)) {
				showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, nameTextField.getText()));
				return;
			}

			projectReference.setName(newProjectName);
			projectList.getItems().setAll(ProjectReference.getProjectsSorted());

			selectProject(projectReference);
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Project_Rename, oldName, e.getMessage()));
		}
	}

	@FXML
	private void newButtonHandler(ActionEvent event) {
		NewProjectDialog dialog = new NewProjectDialog(getStage());
		dialog.getStage().showAndWait();

		Project project = dialog.getProject();
		projectList.getItems().add(project.getProjectReference());
	}

	@FXML
	private void duplicateButtonHandler(ActionEvent event) {
		if (getSelecteItem() != null) {
			DuplicateProjectDialog dialog = new DuplicateProjectDialog(this, getSelectedProject());

			dialog.getName().ifPresent(ref ->
			{
				projectList.getItems().add(ref);
				selectProject(ref);
			});
		}
	}

	@FXML
	private void importButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter(Localization.getString(Strings.File_Filter_ZIP), PlayPadMain.projectZIPType));
		File file = chooser.showOpenDialog(getStage());
		if (file != null) {
			Path zipFile = file.toPath();
			try {
				ImportDialog inportDialog = ImportDialog.getInstance(getStage());
				ProjectReference ref = ProjectImporter.importProject(zipFile, inportDialog, inportDialog);
				if (ref != null) {
					projectList.getItems().add(ref);
					selectProject(ref);
				} else {
					showErrorMessage(Localization.getString(Strings.Error_Project_Open, "null"));
				}
			} catch (IOException | DocumentException e) {
				showErrorMessage(Localization.getString(Strings.Error_Project_Open, e.getLocalizedMessage()));
				e.printStackTrace();
			}
		}
	}

	@FXML
	private void exportButtonHandler(ActionEvent event) {
		ProjectReference selectedProject = getSelectedProject();

		// Speicher das Aktuelle Projekt erst, damit es in der Exportmethode seperat neu geladen werden kann
		if (currentProject.getProjectReference().equals(selectedProject)) {
			try {
				currentProject.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ProjectExportDialog dialog = new ProjectExportDialog(selectedProject, getStage(), this);
		dialog.getStage().show();
	}

	public Optional<ProjectReference> showAndWait() {
		getStage().showAndWait();
		if (!cancel) {
			if (getSelecteItem() != null) {
				if (currentProject.getProjectReference() != getSelecteItem()) {
					return Optional.of(getSelecteItem());
				}
			}
		}
		return Optional.empty();
	}

	private ProjectReference getSelecteItem() {
		return projectList.getSelectionModel().getSelectedItem();
	}

	private ProjectReference getSelectedProject() {
		return getSelecteItem();
	}

	private void selectProject(ProjectReference ref) {
		projectList.getSelectionModel().select(ref);
	}

	@Override
	public void notify(String text, long duration) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration));
		}
	}

	@Override
	public void notify(String text, long duration, Runnable finish) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration, finish);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration, finish));
		}
	}

	@Override
	public void showError(String message) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showError(message);
		} else {
			Platform.runLater(() -> notificationPane.showError(message));
		}
	}

	@Override
	public void hide() {
		if (Platform.isFxApplicationThread()) {
			notificationPane.hide();
		} else {
			Platform.runLater(() -> notificationPane.hide());
		}
	}
}
