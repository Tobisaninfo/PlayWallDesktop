package de.tobias.playpad.plugin.playout.viewcontroller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.itextpdf.text.DocumentException;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.proxy.SettingsProxy;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.playout.Strings;
import de.tobias.playpad.plugin.playout.export.CsvPlayoutLogExport;
import de.tobias.playpad.plugin.playout.export.PlayoutLogPdfExport;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.LogSeasons;
import de.tobias.playpad.plugin.playout.storage.PlayoutLogSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

		logList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		autoStartCheckbox.setSelected(SettingsProxy.getSettings(PlayoutLogSettings.class).autoStartLogging());
		autoStartCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
				SettingsProxy.getSettings(PlayoutLogSettings.class).autoStartLogging(newValue));

		exportButton.setDisable(true);
		logList.getSelectionModel().getSelectedItems().addListener((InvalidationListener) observable ->
				exportButton.setDisable(logList.getSelectionModel().getSelectedItems().size() != 1));
		deleteButton.disableProperty().bind(logList.getSelectionModel().selectedItemProperty().isNull());
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
	private void exportMultipleCsvHandler(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("CSV", "*.csv");
		fileChooser.getExtensionFilters().add(extensionFilter);

		File file = fileChooser.showSaveDialog(getContainingWindow());
		if (file == null) {
			return;
		}
		Path path = file.toPath();

		final LogSeason[] logSeasons = logList.getSelectionModel().getSelectedItems()
				.parallelStream()
				.map(logSeason -> LogSeasons.getLogSeason(logSeason.getId()))
				.toArray(LogSeason[]::new);

		final CsvPlayoutLogExport.CsvColumn[] export = CsvPlayoutLogExport.export(logSeasons);

		// create mapper and schema
		CsvMapper mapper = new CsvMapper();
		mapper.registerModule(new DefaultScalaModule());
		mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

		CsvSchema schema = CsvSchema.builder()
    			.addColumn("Name")
				.addColumn("Zaehler")
				.addColumn("Sessions")
				.addColumn("Erstes Datum")
				.addColumn("Letztes Datum")
				.build();
		schema = schema.withColumnSeparator(';').withHeader();

		// output writer
		ObjectWriter objectWriter = mapper.writer(schema);
		try {
			objectWriter.writeValue(Files.newBufferedWriter(path), export);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		logList.getSelectionModel().getSelectedItems().forEach(season -> {
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
