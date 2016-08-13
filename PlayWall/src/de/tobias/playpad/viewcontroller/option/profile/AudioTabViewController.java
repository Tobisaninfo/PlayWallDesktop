package de.tobias.playpad.viewcontroller.option.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.AudioTypeViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;

public class AudioTabViewController extends ProfileSettingsTabViewController {

	// Audio
	@FXML private ComboBox<String> audioTypeComboBox;
	@FXML private AnchorPane audioUserInfoSettings;
	private AudioTypeViewController audioViewController;
	private boolean changeAudioSettings;

	public AudioTabViewController(boolean playerActive) {
		super("audioTab", "de/tobias/playpad/assets/view/option/profile/", PlayPadMain.getUiResourceBundle());

		if (playerActive) {
			audioTypeComboBox.setDisable(true);
			audioUserInfoSettings.setDisable(true);
		}
	}

	@Override
	public void init() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// Audio Classes
		AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		audioTypeComboBox.getItems().addAll(audioHandlerRegistry.getTypes());

		// Listener for selection
		audioTypeComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			if (b != null && c != null)
				changeAudioSettings = true;
			showAudioSettings(c);
		});
		showAudioSettings(profileSettings.getAudioClass());
	}

	private void showAudioSettings(String classID) {
		if (audioViewController != null) {
			if (audioViewController.isChanged()) {
				changeAudioSettings = true;
			}
		}

		audioUserInfoSettings.getChildren().clear();

		try {
			AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
			audioViewController = audioHandlerRegistry.getComponent(classID).getAudioViewController();

			if (audioViewController != null) {
				audioUserInfoSettings.getChildren().add(audioViewController.getParent());
			}
		} catch (NoSuchComponentException e) {
			e.printStackTrace();
			// TODO Errorhandling
		}
	}

	@Override
	public void loadSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		audioTypeComboBox.getSelectionModel().select(profileSettings.getAudioClass());

	}

	@Override
	public void saveSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		profileSettings.setAudioClass(audioTypeComboBox.getValue());

	}

	@Override
	public boolean needReload() {
		if (audioViewController != null) {
			if (audioViewController.isChanged()) {
				changeAudioSettings = true;
			}
		}
		return changeAudioSettings;
	}

	@Override
	public void reload(Profile profile, ProjectV2 project, IMainViewController controller) {
		Worker.runLater(() ->
		{
			project.loadPadsContent();
		});
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Audio_Title);
	}
}
