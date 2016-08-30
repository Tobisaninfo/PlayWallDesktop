package de.tobias.playpad.viewcontroller.option.global;

import java.io.IOException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.update.UpdateChannel;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.playpad.update.Updates;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.playpad.viewcontroller.cell.UpdateCell;
import de.tobias.playpad.viewcontroller.dialog.UpdaterDialog;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.utils.application.ApplicationInfo;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;

public class UpdateTabViewController extends GlobalSettingsTabViewController {

	@FXML private Label currentVersionLabel;

	@FXML private CheckBox automaticSearchCheckBox;
	@FXML private Button manualSearchButton;

	@FXML private ListView<Updatable> openUpdateList;
	@FXML private Button updateButton;

	@FXML private ComboBox<UpdateChannel> updateChannelComboBox;

	@FXML private Label infoCLabel;
	@FXML private Label infoELabel;

	// Placeholder for List
	private ProgressIndicator progressIndecator;
	private Label placeholderLabel;

	public UpdateTabViewController() {
		super("updateTab", "de/tobias/playpad/assets/view/option/global/", PlayPadMain.getUiResourceBundle());

		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

		updateChannelComboBox.setValue(globalSettings.getUpdateChannel());
		openUpdateList.getItems().setAll(UpdateRegistery.getAvailableUpdates());
		updateButton.setDisable(openUpdateList.getItems().isEmpty());

		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		String currentVersionString = Localization.getString(Strings.UI_Window_Settings_Updates_CurrentVersion, info.getVersion(),
				info.getBuild());
		currentVersionLabel.setText(currentVersionString);
	}

	@Override
	public void init() {
		openUpdateList.setCellFactory(list -> new UpdateCell());
		updateChannelComboBox.getItems().setAll(UpdateChannel.values());
		updateChannelComboBox.setCellFactory(list -> new EnumCell<>(Strings.Update_Channel_BaseName));
		updateChannelComboBox.setButtonCell(new EnumCell<>(Strings.Update_Channel_BaseName));

		updateChannelComboBox.valueProperty().addListener((a, b, c) ->
		{
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			globalSettings.setUpdateChannel(c);
		});

		infoCLabel.setGraphic(new ImageView("de/tobias/playpad/assets/files/class_obj.png"));
		infoELabel.setGraphic(new ImageView("de/tobias/playpad/assets/files/enum_obj.png"));

		progressIndecator = new ProgressIndicator(-1);
		progressIndecator.setMinSize(75, 75);
		progressIndecator.setMaxSize(75, 75);

		placeholderLabel = new Label(Localization.getString(Strings.UI_Placeholder_Updates));
		openUpdateList.setPlaceholder(placeholderLabel);

		updateButton.setDisable(openUpdateList.getItems().isEmpty());
	}

	@FXML
	private void manualSearchHandler(ActionEvent event) {
		openUpdateList.getItems().clear();

		Profile profile = Profile.currentProfile();
		if (profile != null) {
			openUpdateList.setPlaceholder(progressIndecator);

			Worker.runLater(() ->
			{
				// Search for updates
				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
				UpdateRegistery.lookupUpdates(globalSettings.getUpdateChannel());

				Platform.runLater(() ->
				{
					openUpdateList.setPlaceholder(placeholderLabel);
					openUpdateList.getItems().setAll(UpdateRegistery.getAvailableUpdates());
					updateButton.setDisable(openUpdateList.getItems().isEmpty());
				});
			});

		}
	}

	@FXML
	private void updateHandler(ActionEvent event) {
		UpdaterDialog dialog = new UpdaterDialog(getStage());
		dialog.show();

		GlobalSettings settings = PlayPadPlugin.getImplementation().getGlobalSettings();
		settings.setIgnoreUpdate(false);
		
		Worker.runLater(() ->
		{
			try {
				Updates.startUpdate();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
