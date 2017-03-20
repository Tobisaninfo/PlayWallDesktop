package de.tobias.playpad.viewcontroller.dialog.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.importer.ProjectImporter;
import de.tobias.playpad.project.importer.ProjectImporterDelegate;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.utils.nui.BusyView;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by tobias on 11.03.17.
 */
public class ProjectImportDialog extends NVC implements ProjectImporterDelegate {

	@FXML private TextField projectNameTextField;
	@FXML private CheckBox syncCheckbox;

	@FXML private VBox profileSection;
	@FXML private CheckBox profileImportCheckbox;
	@FXML private TextField profileNameTextField;

	@FXML private VBox mediaSection;
	@FXML private CheckBox mediaImportCheckbox;
	@FXML private Button mediaPathButton;
	@FXML private Label mediaPathLabel;

	@FXML private Button cancelButton;
	@FXML private Button importButton;

	private BusyView busyView;

	private ProjectImporter importer;
	private Path mediaPath;

	public ProjectImportDialog(Path path, Window owner) throws IOException, DocumentException {
		load("de/tobias/playpad/assets/dialog/project/", "importDialog", PlayPadMain.getUiResourceBundle());
		applyViewControllerToStage().initOwner(owner);

		importer = new ProjectImporter(path, this);

		// Set Default Values
		projectNameTextField.setText(importer.getProjectName());
		profileNameTextField.setText(importer.getProfileName());

		profileSection.setDisable(!importer.isIncludeProfile());
		profileImportCheckbox.setSelected(importer.isIncludeProfile());

		mediaSection.setDisable(!importer.isIncludeMedia());
		mediaImportCheckbox.setSelected(importer.isIncludeMedia());

		// Init Busy View
		busyView = new BusyView(this);

		if (importer.isIncludeMedia()) {
			importButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		mediaImportCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue && mediaPath == null) {
				importButton.setDisable(true);
			} else {
				importButton.setDisable(false);
			}
		});
	}

	public Optional<ProjectReference> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
		if (canceled)
			return Optional.empty();
		else
			return Optional.of(importer.getProjectReference());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(320);
		stage.setMinHeight(480);
		stage.setWidth(320);
		stage.setHeight(480);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_ProjectImport_Title));

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null)
			Profile.currentProfile().currentLayout().applyCss(stage);
	}

	// ActionHandler
	@FXML
	void mediaPathHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getContainingWindow());
		if (folder != null) {
			mediaPath = folder.toPath();
			mediaPathLabel.setText(mediaPath.toString());

			importButton.setDisable(false);
		}
	}

	private boolean canceled;

	@FXML
	void cancelHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
		canceled = true;
	}

	@FXML
	void importHandler(ActionEvent event) {
		busyView.getIndicator().setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		busyView.showProgress(true);

		Worker.runLater(() -> {
			try {
				importer.execute();
				canceled = false;

				Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
			} catch (IOException | DocumentException | ProjectNotFoundException | ProfileNotFoundException e) {
				e.printStackTrace();
			}
		});
	}


	// Import Delegate
	@Override
	public String getProjectName() {
		return projectNameTextField.getText();
	}

	@Override
	public boolean shouldProjectSynced() {
		return syncCheckbox.isSelected();
	}

	@Override
	public boolean shouldImportProfile() {
		return profileImportCheckbox.isSelected();
	}

	@Override
	public String getProfileName() {
		return profileNameTextField.getText();
	}

	@Override
	public boolean shouldImportMedia() {
		return mediaImportCheckbox.isSelected();
	}

	@Override
	public Path getMediaPath() {
		return mediaPath;
	}
}
