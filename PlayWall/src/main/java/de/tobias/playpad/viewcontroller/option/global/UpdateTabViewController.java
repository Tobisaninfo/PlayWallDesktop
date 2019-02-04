package de.tobias.playpad.viewcontroller.option.global;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationInfo;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.versionizer.model.Version;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.PlayPad;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.playpad.viewcontroller.cell.UpdateCell;
import de.tobias.playpad.viewcontroller.dialog.UpdaterDialog;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class UpdateTabViewController extends GlobalSettingsTabViewController {

	@FXML
	private Label currentVersionLabel;

	@FXML
	private CheckBox automaticSearchCheckBox;
	@FXML
	private Button manualSearchButton;

	@FXML
	private ListView<Version> openUpdateList;
	@FXML
	private Button updateButton;

	@FXML
	private ComboBox<UpdateService.RepositoryType> updateChannelComboBox;

	@FXML
	private Label infoCLabel;
	@FXML
	private Label infoELabel;

	// Placeholder for List
	private ProgressIndicator progressIndicator;
	private Label placeholderLabel;

	UpdateTabViewController() {
		load("view/option/global", "UpdateTab", PlayPadMain.getUiResourceBundle());

		final PlayPad playPad = PlayPadPlugin.getImplementation();
		GlobalSettings globalSettings = playPad.getGlobalSettings();

		updateChannelComboBox.setValue(globalSettings.getUpdateChannel());
		openUpdateList.getItems().setAll(playPad.getUpdateService().getRemoteVersions().values());
		updateButton.setDisable(openUpdateList.getItems().isEmpty());

		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		String currentVersionString = Localization.getString(Strings.UI_Window_Settings_Updates_CurrentVersion, info.getVersion(),
				info.getBuild());
		currentVersionLabel.setText(currentVersionString);
	}

	@Override
	public void init() {
		openUpdateList.setCellFactory(list -> new UpdateCell());
		updateChannelComboBox.getItems().setAll(UpdateService.RepositoryType.RELEASE, UpdateService.RepositoryType.SNAPSHOT);
		updateChannelComboBox.setCellFactory(list -> new EnumCell<>(Strings.Update_Channel_BaseName));
		updateChannelComboBox.setButtonCell(new EnumCell<>(Strings.Update_Channel_BaseName));

		updateChannelComboBox.valueProperty().addListener((a, b, c) ->
		{
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			globalSettings.setUpdateChannel(c);
		});

		infoCLabel.setGraphic(new ImageView("gfx/class_obj.png"));
		infoELabel.setGraphic(new ImageView("gfx/enum_obj.png"));

		progressIndicator = new ProgressIndicator(-1);
		progressIndicator.setMinSize(75, 75);
		progressIndicator.setMaxSize(75, 75);

		placeholderLabel = new Label(Localization.getString(Strings.UI_Placeholder_Updates));
		openUpdateList.setPlaceholder(placeholderLabel);

		updateButton.setDisable(openUpdateList.getItems().isEmpty());
	}

	@FXML
	private void manualSearchHandler(ActionEvent event) {
		openUpdateList.getItems().clear();

		Profile profile = Profile.currentProfile();
		if (profile != null) {
			openUpdateList.setPlaceholder(progressIndicator);

			Worker.runLater(() ->
			{
				final UpdateService updateService = PlayPadPlugin.getImplementation().getUpdateService();

				// Search for updates
				updateService.fetchCurrentVersion();

				Platform.runLater(() ->
				{
					openUpdateList.setPlaceholder(placeholderLabel);
					openUpdateList.getItems().setAll(updateService.getRemoteVersions().values());
					updateButton.setDisable(openUpdateList.getItems().isEmpty());
				});
			});

		}
	}

	@SuppressWarnings("Duplicates")
	@FXML
	private void updateHandler(ActionEvent event) {
		UpdaterDialog dialog = new UpdaterDialog(getContainingWindow());
		dialog.show();

		GlobalSettings settings = PlayPadPlugin.getImplementation().getGlobalSettings();
		settings.setIgnoreUpdate(false);

		Worker.runLater(() ->
		{
			final UpdateService updateService = PlayPadPlugin.getImplementation().getUpdateService();

			Logger.info("Install update");
			try {
				updateService.runVersionizerInstance(updateService.getAllLatestVersionEntries());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		});
	}

	// Settings Tab Methods
	@Override
	public void loadSettings(GlobalSettings profile) {
		automaticSearchCheckBox.setSelected(profile.isAutoUpdate());
	}

	@Override
	public void saveSettings(GlobalSettings profile) {
		profile.setAutoUpdate(automaticSearchCheckBox.isSelected());
	}

	@Override
	public boolean needReload() {
		return false;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Updates_Title);
	}
}
