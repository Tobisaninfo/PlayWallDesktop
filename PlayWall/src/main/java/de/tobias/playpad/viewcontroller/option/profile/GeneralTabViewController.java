package de.tobias.playpad.viewcontroller.option.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.ui.Alertable;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.NumberUtils;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneralTabViewController extends GlobalSettingsTabViewController {

	@FXML
	private CheckBox openLastDocumentCheckbox;

	@FXML
	private CheckBox liveModeCheckBox;

	@FXML
	private TextField cacheTextField;
	@FXML
	private Label cacheSizeLabel;

	@FXML
	private RadioButton pageEnable;
	@FXML
	private RadioButton pageDisable;
	@FXML
	private RadioButton dragEnable;
	@FXML
	private RadioButton dragDisable;
	@FXML
	private RadioButton fileEnable;
	@FXML
	private RadioButton fileDisable;
	@FXML
	private RadioButton settingsEnable;
	@FXML
	private RadioButton settingsDisable;

	@FXML
	private CheckBox enableAutosaveCheckbox;
	@FXML
	private TextField autosaveIntervalTextField;

	private static final String DIGIT_POSITIVE = "^[1-9]\\d*$";

	private Alertable alertable;

	public GeneralTabViewController(Alertable alertable) {
		load("view/option/global", "GeneralTab", Localization.getBundle());
		this.alertable = alertable;

		calcCacheSize();
	}

	@Override
	public void init() {
		ToggleGroup pageGroup = new ToggleGroup();
		pageGroup.getToggles().addAll(pageEnable, pageDisable);
		ToggleGroup dragGroup = new ToggleGroup();
		dragGroup.getToggles().addAll(dragEnable, dragDisable);
		ToggleGroup fileGroup = new ToggleGroup();
		fileGroup.getToggles().addAll(fileEnable, fileDisable);
		ToggleGroup settingsGroup = new ToggleGroup();
		settingsGroup.getToggles().addAll(settingsEnable, settingsDisable);

		liveModeCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> disableLiveSettings(newValue));
		enableAutosaveCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> autosaveIntervalTextField.setDisable(!newValue));
	}

	private void disableLiveSettings(Boolean enableLiveSettings) {
		pageEnable.setDisable(!enableLiveSettings);
		pageDisable.setDisable(!enableLiveSettings);
		dragEnable.setDisable(!enableLiveSettings);
		dragDisable.setDisable(!enableLiveSettings);
		fileEnable.setDisable(!enableLiveSettings);
		fileDisable.setDisable(!enableLiveSettings);
		settingsEnable.setDisable(!enableLiveSettings);
		settingsDisable.setDisable(!enableLiveSettings);
	}

	@FXML
	private void cacheChooseHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getContainingWindow());
		if (folder != null) {
			Path folderPath = folder.toPath();
			GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
			globalSettings.setCachePath(folderPath);
			cacheTextField.setText(folderPath.toString());
		}
	}

	@FXML
	private void cacheResetButtonHandler(ActionEvent event) {
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(ApplicationUtils.getApplication().getPath(PathType.CACHE))) {
			int deleteFiles = 0;
			for (Path path : directoryStream) {
				if (Files.isRegularFile(path)) {
					Files.delete(path);
					deleteFiles++;
				}
			}
			alertable.showInfoMessage(Localization.getString(Strings.INFO_SETTINGS_CACHE_DELETE, deleteFiles));

			calcCacheSize();
		} catch (IOException e) {
			Logger.error(e);
			showErrorMessage(Localization.getString(Strings.ERROR_SETTINGS_CACHE_CLEAR, e.getLocalizedMessage()));
		}
	}

	@FXML
	private void resetDialogs(ActionEvent event) {
		alertable.showInfoMessage(Localization.getString(Strings.INFO_SETTINGS_RESET_WARNING));
	}

	private void calcCacheSize() {
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(globalSettings.getCachePath())) {
			double size = 0;
			Path path = globalSettings.getCachePath();
			if (Files.notExists(path))
				Files.createDirectories(path);

			for (Path item : directoryStream) {
				size += Files.size(item);
			}
			cacheSizeLabel.setText(Localization.getString(Strings.UI_WINDOW_SETTINGS_GEN_CACHE_SIZE, NumberUtils.convertBytesToAppropriateFormat(size)));
		} catch (IOException e) {
			Logger.error(e);
			alertable.showErrorMessage(Localization.getString(Strings.ERROR_SETTINGS_CACHE_SIZE, e.getMessage()));
		}
	}

	@Override
	public void loadSettings(GlobalSettings settings) {
		openLastDocumentCheckbox.setSelected(settings.isOpenLastDocument());

		liveModeCheckBox.setSelected(settings.isLiveMode());
		cacheTextField.setText(settings.getCachePath().toString());

		if (settings.isLiveModePage())
			pageEnable.setSelected(true);
		else
			pageDisable.setSelected(true);

		if (settings.isLiveModeDrag())
			dragEnable.setSelected(true);
		else
			dragDisable.setSelected(true);

		if (settings.isLiveModeFile())
			fileEnable.setSelected(true);
		else
			fileDisable.setSelected(true);

		if (settings.isLiveModeSettings())
			settingsEnable.setSelected(true);
		else
			settingsDisable.setSelected(true);

		disableLiveSettings(settings.isLiveMode());

		enableAutosaveCheckbox.setSelected(settings.isEnableAutosave());
		autosaveIntervalTextField.setText(String.valueOf(settings.getAutosaveIntervalInMinutes()));
		autosaveIntervalTextField.setDisable(!settings.isEnableAutosave());
		autosaveIntervalTextField.textProperty().addListener((a, b, c) -> autosaveIntervalTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, !c.matches(DIGIT_POSITIVE) || c.isEmpty()));
	}

	@Override
	public void saveSettings(GlobalSettings settings) {
		settings.setOpenLastDocument(openLastDocumentCheckbox.isSelected());

		settings.setLiveMode(liveModeCheckBox.isSelected());
		settings.setCachePath(Paths.get(cacheTextField.getText()));

		settings.setLiveModePage(pageEnable.isSelected());
		settings.setLiveModeDrag(dragEnable.isSelected());
		settings.setLiveModeFile(fileEnable.isSelected());
		settings.setLiveModeSettings(settingsEnable.isSelected());

		settings.setEnableAutosave(enableAutosaveCheckbox.isSelected());
		settings.setAutosaveIntervalInMinutes(Integer.parseInt(autosaveIntervalTextField.getText()));
	}

	@Override
	public boolean needReload() {
		return false;
	}

	@Override
	public boolean validSettings() {
		try {
			final int autosaveInterval = Integer.parseInt(autosaveIntervalTextField.getText());
			return autosaveInterval > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_WINDOW_SETTINGS_GEN_TITLE);
	}
}
