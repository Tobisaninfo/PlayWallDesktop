package de.tobias.playpad.viewcontroller.dialog.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.viewcontroller.cell.ProjectCell;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.Optional;

/**
 * Created by tobias on 26.02.17.
 */
public class ProjectManagerDialogV2 extends NVC {

	@FXML private ListView<ProjectReference> projectList;

	@FXML private ComboBox<ProfileReference> profileCombobox;
	@FXML private TextField nameTextfield;

	@FXML private CheckBox syncCheckbox;
	@FXML private Button syncSettingsButton;

	@FXML private Button projectExportButton;
	@FXML private Button projectDeleteButton;

	@FXML private Button cancelButton;
	@FXML private Button openButton;

	public ProjectManagerDialogV2(Window owner, Project currentProject) {
		load("de/tobias/playpad/assets/dialog/project", "projectManagementDialog.fxml", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@Override
	public void init() {
		projectList.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Project)));
		projectList.setCellFactory(list -> new ProjectCell(false));

		// Set Items
		projectList.getItems().setAll(ProjectReferenceManager.getProjectsSorted());
		profileCombobox.getItems().setAll(ProfileReferenceManager.getProfiles());

		// Mouse Open Handler
		projectList.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
				if (mouseEvent.getClickCount() == 2) {
					if (!projectList.getSelectionModel().isEmpty()) {
						openHandler(null);
					}
				}
			}
		});

		// Select Listener
		projectList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				setSettingsDisable(true);

				nameTextfield.clear();
				profileCombobox.setValue(null);
			} else {
				setSettingsDisable(false);

				nameTextfield.setText(newValue.getName());
				profileCombobox.setValue(newValue.getProfileReference());
			}
		});

		// Name Change Listener
		nameTextfield.textProperty().addListener((observable, oldValue, newValue) -> {
			ProjectReference reference = getSelectedItem();
			if (reference != null) {
				reference.setName(newValue);
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
					showErrorMessage(Localization.getString(Strings.Error_Project_Sync_Change, e.getLocalizedMessage()));
					e.printStackTrace();
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
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setWidth(600);
		stage.setHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_ProjectManager_Title));

		stage.initModality(Modality.WINDOW_MODAL);

		Profile.currentProfile().currentLayout().applyCss(stage);
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
	private void projectExportHandler(ActionEvent event) {
		// TODO Implement Project Export
		showInfoMessage("Easter Egg");
	}

	@FXML
	private void projectDeleteHandler(ActionEvent event) {
		ProjectReference reference = getSelectedItem();
		if (reference != null) {

			Alert dialog = new ProjectDeleteDialog(getContainingWindow());
			Optional<ButtonType> result = dialog.showAndWait();
			result.filter(t -> t == ButtonType.YES).ifPresent(t -> {
				try {
					ProjectReferenceManager.removeProject(reference);
				} catch (IOException e) {
					showErrorMessage(Localization.getString(Strings.Error_Project_Delete, e.getLocalizedMessage()));
					e.printStackTrace();
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
