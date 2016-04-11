package de.tobias.playpad.viewcontroller.dialog;

import java.io.File;
import java.io.IOException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProjectExporter;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.scene.BusyView;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ExportDialog extends ViewController {

	@FXML private CheckBox profileCheckBox;
	@FXML private CheckBox mediaCheckBox;

	@FXML private Button cancelButton;
	@FXML private Button saveButton;

	private BusyView busyView;

	private ProjectReference projectRef;
	private NotificationHandler notificationHandler;

	public ExportDialog(ProjectReference projectRef, Window owner, NotificationHandler notificationHandler) {
		super("exportDialog", "de/tobias/playpad/assets/dialog/project/", null, PlayPadMain.getUiResourceBundle());
		this.projectRef = projectRef;
		this.notificationHandler = notificationHandler;

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);

		busyView = new BusyView(this);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_ProjectExport_Title));
		stage.setWidth(320);
		stage.setHeight(180);

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void saveButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters()
				.add(new ExtensionFilter(Localization.getString(Strings.File_Filter_ZIP), PlayPadMain.projectCompressedType));
		File file = chooser.showSaveDialog(getStage());
		if (file != null) {
			cancelButton.setDisable(true);
			busyView.showProgress(true);

			Worker.runLater(() ->
			{
				try {
					ProjectExporter.exportProject(projectRef, file.toPath(), profileCheckBox.isSelected(), mediaCheckBox.isSelected());

					Platform.runLater(() ->
					{
						busyView.showProgress(false);
						getStage().close();
						notificationHandler.notify(Localization.getString(Strings.Standard_File_Save),
								PlayPadMain.notificationDisplayTimeMillis);
					});
				} catch (IOException e) {
					e.printStackTrace();
					Platform.runLater(() ->
					{
						busyView.showProgress(false);
						showErrorMessage(Localization.getString(Strings.Error_Project_Export, projectRef.getName(), e.getMessage()),
								PlayPadMain.stageIcon);
					});
				}
			});
		}
	}
}
