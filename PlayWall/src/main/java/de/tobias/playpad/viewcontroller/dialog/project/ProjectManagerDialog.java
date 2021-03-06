package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.importer.ProjectImporter;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by tobias on 26.02.17.
 */
public class ProjectManagerDialog extends NVC {

	@FXML
	private ListView<ProjectReference> projectList;

	@FXML
	private ComboBox<ProfileReference> profileCombobox;
	@FXML
	private TextField nameTextfield;

	@FXML
	private CheckBox syncCheckbox;
	@FXML
	private Button syncSettingsButton;

	@FXML
	private Button projectExportButton;
	@FXML
	private Button projectImportButton;
	@FXML
	private Button projectDuplicateButton;
	@FXML
	private Button projectDeleteButton;

	@FXML
	private Button cancelButton;
	@FXML
	private Button openButton;

	public ProjectManagerDialog(Window owner) {
		load("view/dialog/project", "ProjectManagementDialog.fxml", Localization.getBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@Override
	public void init() {
		projectList.setPlaceholder(new Label(Localization.getString(Strings.UI_PLACEHOLDER_PROJECT)));
		projectList.setCellFactory(list -> new ProjectCell(false));

		// Set Items
		projectList.getItems().setAll(ProjectReferenceManager.getProjectsSorted());
		profileCombobox.getItems().setAll(ProfileReferenceManager.getProfiles());

		// Mouse Open Handler
		projectList.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
				if (!projectList.getSelectionModel().isEmpty()) {
					openHandler(null);
				}
			}
		});

		// Initial Value
		projectExportButton.setDisable(true);
		projectDuplicateButton.setDisable(true);
		projectDeleteButton.setDisable(true);

		// Select Listener
		projectList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				setSettingsDisable(true);

				nameTextfield.clear();
				profileCombobox.setValue(null);

				projectExportButton.setDisable(true);
				projectDuplicateButton.setDisable(true);
				projectDeleteButton.setDisable(true);
			} else {
				setSettingsDisable(false);

				nameTextfield.setText(newValue.getName());
				profileCombobox.setValue(newValue.getProfileReference());

				projectExportButton.setDisable(false);
				projectDuplicateButton.setDisable(false);
				projectDeleteButton.setDisable(false);
			}
		});

		// Name Change Listener
		nameTextfield.textProperty().addListener((observable, oldValue, newValue) -> {
			if (ProjectReferenceManager.validateProjectName(newValue)) {
				ProjectReference reference = getSelectedItem();
				if (reference != null && ProjectReferenceManager.validateProjectName(reference, newValue)) {
					reference.setName(newValue);
					nameTextfield.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
				} else {
					nameTextfield.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
				}
			}
		});

		profileCombobox.valueProperty().addListener((observable, oldValue, newValue) -> {
			ProjectReference reference = getSelectedItem();
			if (reference != null) {
				reference.setProfileReference(newValue);
			}
		});

		syncCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			ProjectReference reference = getSelectedItem();
			if (reference != null) {
				try {
					ProjectReferenceManager.setSync(reference, newValue);
				} catch (ProjectNotFoundException | ProfileNotFoundException | DocumentException | IOException e) {
					showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_SYNC_CHANGE, e.getLocalizedMessage()));
					Logger.error(e);
				} catch (ProjectReader.ProjectReaderDelegate.ProfileAbortException ignored) {
				}
			}
		});
	}

	private void setSettingsDisable(boolean disable) {
		nameTextfield.setDisable(disable);
		profileCombobox.setDisable(disable);
		syncCheckbox.setDisable(disable);

		projectExportButton.setDisable(disable);
		projectDeleteButton.setDisable(disable);

		openButton.setDisable(disable);
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(600);
		stage.setMinHeight(560);
		stage.setWidth(600);
		stage.setHeight(540);
		stage.setTitle(Localization.getString(Strings.UI_DIALOG_PROJECT_MANAGER_TITLE));

		stage.initModality(Modality.WINDOW_MODAL);

		PlayPadPlugin.styleable().applyStyle(stage);
	}

	public Optional<ProjectReference> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);

		if (open) {
			return Optional.ofNullable(getSelectedItem());
		}

		return Optional.empty();
	}

	@FXML
	private void syncSettingsHandler(ActionEvent event) {
		// TODO Implement Settings View
		showInfoMessage("Easter Egg");
	}

	@FXML
	private void projectImportHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();

		String extensionName = Localization.getString(Strings.FILE_FILTER_ZIP);
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(extensionName, PlayPadMain.ZIP_TYPE);
		chooser.getExtensionFilters().add(extensionFilter);

		File file = chooser.showOpenDialog(getContainingWindow());

		if (file != null) {
			try {
				ProjectImportDialog dialog = new ProjectImportDialog(file.toPath(), getContainingWindow());
				Optional<ProjectReference> importedProject = dialog.showAndWait();
				importedProject.ifPresent(projectList.getItems()::add);
			} catch (IOException | ProjectImporter.ProjectImportCorruptedException e) {
				Logger.error(e);
			}
		}
	}

	@FXML
	private void projectExportHandler(ActionEvent event) {
		ProjectReference reference = getSelectedItem();
		if (reference != null) {
			ProjectExportDialog dialog = new ProjectExportDialog(reference, getContainingWindow());
			dialog.getStageContainer().ifPresent(NVCStage::showAndWait);
		}
	}

	@FXML
	private void projectDuplicateHandler(ActionEvent event) {
		ProjectReference reference = getSelectedItem();
		if (reference != null) {
			ProjectDuplicateDialog projectDuplicateDialog = new ProjectDuplicateDialog(this, reference);
			Optional<ProjectReference> name = projectDuplicateDialog.getName();
			name.ifPresent(projectList.getItems()::add);
		}
	}

	@FXML
	private void projectDeleteHandler(ActionEvent event) {
		ProjectReference reference = getSelectedItem();
		if (reference != null) {

			Alert dialog = new ProjectDeleteDialog(reference, getContainingWindow());
			Optional<ButtonType> result = dialog.showAndWait();
			result.filter(t -> t == ButtonType.OK).ifPresent(t -> {
				try {
					ProjectReferenceManager.removeProject(reference);
					projectList.getItems().remove(reference);
				} catch (IOException e) {
					showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_DELETE, e.getLocalizedMessage()));
					Logger.error(e);
				}
			});

		}
	}

	private ProjectReference getSelectedItem() {
		return projectList.getSelectionModel().getSelectedItem();
	}

	private boolean open = false;

	@FXML
	private void cancelHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void openHandler(ActionEvent event) {
		open = true;
		getStageContainer().ifPresent(NVCStage::close);
	}
}
