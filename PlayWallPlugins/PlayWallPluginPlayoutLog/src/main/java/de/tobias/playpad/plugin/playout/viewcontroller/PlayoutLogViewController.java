package de.tobias.playpad.plugin.playout.viewcontroller;

import com.itextpdf.text.DocumentException;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.proxy.SettingsProxy;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.playout.Strings;
import de.tobias.playpad.plugin.playout.export.PlayoutLogPdfExport;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.LogSeasons;
import de.tobias.playpad.plugin.playout.storage.PlayoutLogSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class PlayoutLogViewController extends NVC {

	@FXML
	private TextField nameTextField;
	@FXML
	private ListView<LogSeason> logList;
	@FXML
	private Button startButton;
	@FXML
	private Button exportButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button finishButton;
	@FXML
	private CheckBox autoStartCheckbox;

	public PlayoutLogViewController(Window owner) {
		load("view/dialog", "PlayoutLogDialog", Localization.getBundle());

		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(stage::close);
	}

	@Override
	public void init() {
		logList.getItems().setAll(LogSeasons.getAllLogSeasonsLazy());

		if (LogSeasons.getCurrentSession() != null) { // Running
			startButton.setText(Localization.getString(Strings.PLAYOUT_LOG_DIALOG_BUTTON_STOP));
			nameTextField.setDisable(true);
			nameTextField.setText(LogSeasons.getCurrentSession().getName());
		} else {
			startButton.setText(Localization.getString(Strings.PLAYOUT_LOG_DIALOG_BUTTON_START));
			nameTextField.setDisable(false);
		}

		autoStartCheckbox.setSelected(SettingsProxy.getSettings(PlayoutLogSettings.class).autoStartLogging());
		autoStartCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
				SettingsProxy.getSettings(PlayoutLogSettings.class).autoStartLogging(newValue));
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setTitle(Localization.getString(Strings.UI_DIALOG_PLAYOUT_LOG_TITLE));
		stage.setMinWidth(450);
		stage.setMinHeight(600);

		stage.initModality(Modality.WINDOW_MODAL);

		PlayPadPlugin.styleable().applyStyle(stage);
	}

	@FXML
	private void startButtonHandler(ActionEvent event) {
		if (LogSeasons.getCurrentSession() == null) { // Start
			if (nameTextField.getText().isEmpty()) {
				return;
			}

			final Project currentProject = PlayPadPlugin.getInstance().getCurrentProject();
			final ProjectSettings settings = currentProject.getSettings();

			LogSeason logSeason = LogSeasons.createLogSeason(nameTextField.getText(), settings.getColumns(), settings.getRows());
			logSeason.createProjectSnapshot(currentProject);

			startButton.setText(Localization.getString(Strings.PLAYOUT_LOG_DIALOG_BUTTON_STOP));
			nameTextField.setDisable(false);
		} else { // Stop
			LogSeasons.stop();
			startButton.setText(Localization.getString(Strings.PLAYOUT_LOG_DIALOG_BUTTON_START));
			nameTextField.setDisable(false);
		}
	}

	@FXML
	private void exportButtonHandler(ActionEvent event) {
		getSelectedLogSeason().ifPresent(season -> { // Lazy Season
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
			fileChooser.getExtensionFilters().add(extensionFilter);

			File file = fileChooser.showSaveDialog(getContainingWindow());
			if (file != null) {
				try {
					PlayoutLogPdfExport.createPdfFile(file.toPath(), LogSeasons.getLogSeason(season.getId()));
				} catch (IOException | DocumentException e) {
					Logger.error(e);
				}
			}
		});
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		getSelectedLogSeason().ifPresent(season -> { // Lazy Season
			LogSeasons.deleteSession(season.getId());
			logList.getItems().remove(season);
		});
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	private Optional<LogSeason> getSelectedLogSeason() {
		return Optional.ofNullable(logList.getSelectionModel().getSelectedItem());
	}
}
