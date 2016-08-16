package de.tobias.playpad.viewcontroller.option.profile;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.Alertable;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.NumberUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;

public class GeneralTabViewController extends GlobalSettingsTabViewController {

	@FXML private CheckBox liveModeCheckBox;

	@FXML private TextField cacheTextField;
	@FXML private Label cacheSizeLabel;

	@FXML private RadioButton pageEnable;
	@FXML private RadioButton pageDisable;
	@FXML private ToggleGroup pageGroup;
	@FXML private RadioButton dragEnable;
	@FXML private RadioButton dragDisable;
	@FXML private ToggleGroup dragGroup;
	@FXML private RadioButton fileEnable;
	@FXML private RadioButton fileDisable;
	@FXML private ToggleGroup fileGroup;
	@FXML private RadioButton settingsEnable;
	@FXML private RadioButton settingsDisable;
	@FXML private ToggleGroup settingsGroup;

	private boolean changeSettings;

	private Alertable alertable;

	public GeneralTabViewController(Alertable alertable) {
		super("generalTab", "de/tobias/playpad/assets/view/option/global/", PlayPadMain.getUiResourceBundle());
		this.alertable = alertable;

		calcCacheSize();
	}

	@Override
	public void init() {
		pageGroup = new ToggleGroup();
		pageGroup.getToggles().addAll(pageEnable, pageDisable);
		dragGroup = new ToggleGroup();
		dragGroup.getToggles().addAll(dragEnable, dragDisable);
		fileGroup = new ToggleGroup();
		fileGroup.getToggles().addAll(fileEnable, fileDisable);
		settingsGroup = new ToggleGroup();
		settingsGroup.getToggles().addAll(settingsEnable, settingsDisable);

		liveModeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			disableLiveSettings(c);
		});
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
		File folder = chooser.showDialog(getStage());
		if (folder != null) {
			Path folderPath = folder.toPath();
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			globalSettings.setCachePath(folderPath);
			cacheTextField.setText(folderPath.toString());
		}
	}

	@FXML
	private void cacheResetButtonHandler(ActionEvent event) {
		try {
			int deleteFiles = 0;
			for (Path path : Files.newDirectoryStream(ApplicationUtils.getApplication().getPath(PathType.CACHE))) {
				if (Files.isRegularFile(path)) {
					try {
						Files.delete(path);
						deleteFiles++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			alertable.showInfoMessage(Localization.getString(Strings.Info_Settings_CacheDelete, deleteFiles), PlayPadMain.stageIcon.get());

			calcCacheSize();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Settings_CacheClear, e.getLocalizedMessage()));
		}
	}

	@FXML
	private void resetDialogs(ActionEvent event) {
		alertable.showInfoMessage(Localization.getString(Strings.Info_Settings_ResetWarning));
	}

	private void calcCacheSize() {
		try {
			double size = 0;
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			Path path = globalSettings.getCachePath();
			if (Files.notExists(path))
				Files.createDirectories(path);

			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(globalSettings.getCachePath());
			for (Path item : directoryStream) {
				size += Files.size(item);
			}
			directoryStream.close();
			cacheSizeLabel.setText(Localization.getString(Strings.UI_Window_Settings_Gen_CacheSize, NumberUtils.numberToString(size)));
		} catch (IOException e) {
			e.printStackTrace();
			alertable.showErrorMessage(Localization.getString(Strings.Error_Settings_CacheSize, e.getMessage()), PlayPadMain.stageIcon);
		}
	}

	@Override
	public void loadSettings(GlobalSettings settings) {

		liveModeCheckBox.setSelected(settings.isLiveMode());
		cacheTextField.setText(settings.getCachePath().toString());

		if (settings.isLiveModePage() == true)
			pageEnable.setSelected(true);
		else
			pageDisable.setSelected(true);

		if (settings.isLiveModeDrag() == true)
			dragEnable.setSelected(true);
		else
			dragDisable.setSelected(true);

		if (settings.isLiveModeFile() == true)
			fileEnable.setSelected(true);
		else
			fileDisable.setSelected(true);

		if (settings.isLiveModeSettings() == true)
			settingsEnable.setSelected(true);
		else
			settingsDisable.setSelected(true);

		disableLiveSettings(settings.isLiveMode());
	}

	@Override
	public void saveSettings(GlobalSettings settings) {
		settings.setLiveMode(liveModeCheckBox.isSelected());
		settings.setCachePath(Paths.get(cacheTextField.getText()));

		settings.setLiveModePage(pageEnable.isSelected());
		settings.setLiveModeDrag(dragEnable.isSelected());
		settings.setLiveModeFile(fileEnable.isSelected());
		settings.setLiveModeSettings(settingsEnable.isSelected());
	}

	@Override
	public boolean needReload() {
		return changeSettings;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Gen_Title);
	}
}
