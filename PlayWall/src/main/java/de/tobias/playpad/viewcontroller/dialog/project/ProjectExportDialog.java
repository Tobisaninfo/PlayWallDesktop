package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.export.ProjectExporter;
import de.tobias.playpad.project.export.ProjectExporterDelegate;
import de.tobias.playpad.project.ref.ProjectReference;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ProjectExportDialog extends NVC implements ProjectExporterDelegate {

	@FXML
	private CheckBox profileCheckBox;
	@FXML
	private CheckBox mediaCheckBox;

	@FXML
	private Button cancelButton;
	@FXML
	private Button saveButton;

	private BusyView busyView;

	private ProjectReference projectRef;

	ProjectExportDialog(ProjectReference projectRef, Window owner) {
		load("view/dialog/project", "ExportDialog", Localization.getBundle());
		this.projectRef = projectRef;

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);

		busyView = new BusyView(this);
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setTitle(Localization.getString(Strings.UI_Dialog_ProjectExport_Title));
		stage.setWidth(375);
		stage.setHeight(180);
		stage.initModality(Modality.WINDOW_MODAL);

		PlayPadPlugin.styleable().applyStyle(stage);
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void saveButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();

		// Extension Filter in FileChooser
		String extensionName = Localization.getString(Strings.File_Filter_ZIP);
		ExtensionFilter extensionFilter = new ExtensionFilter(extensionName, PlayPadMain.ZIP_TYPE);
		chooser.getExtensionFilters().add(extensionFilter);

		File file = chooser.showSaveDialog(getContainingWindow());
		if (file != null) {
			cancelButton.setDisable(true);

			busyView.getIndicator().setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
			busyView.showProgress(true);

			Worker.runLater(() ->
			{
				try {
					Path path = file.toPath();
					boolean includeProfile = profileCheckBox.isSelected();
					boolean includeMedia = mediaCheckBox.isSelected();

					ProjectExporter exporter = new ProjectExporter(this);
					exporter.export(path, projectRef, includeProfile, includeMedia);

					Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
				} catch (IOException e) {
					busyView.showProgress(false);

					String errorMessage = Localization.getString(Strings.Error_Project_Export, projectRef.getName(), e.getMessage());
					showErrorMessage(errorMessage);
					Logger.error(e);
				} catch (ProjectNotFoundException | DocumentException | ProfileNotFoundException e) {
					Logger.error(e);
				} catch (ProjectReader.ProjectReaderDelegate.ProfileAbortException ignored) {
				}
			});
		}
	}

	private int tasks;
	private int complete;

	@Override
	public void setTasks(int value) {
		this.tasks = value;
		complete = 0;
	}

	@Override
	public void taskComplete() {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(this::taskComplete);
			return;
		}
		complete++;
		busyView.getIndicator().setProgress((float) complete / (float) tasks);
	}
}
