package de.tobias.playpad.viewcontroller.option.profile;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AudioTabViewController extends ProfileSettingsTabViewController implements IProfileReloadTask {

	// Audio
	@FXML private ComboBox<String> audioTypeComboBox;
	@FXML private VBox options;

	private List<AudioHandlerViewController> audioViewController;
	private boolean changeAudioSettings;

	public AudioTabViewController(boolean playerActive) {
		super("audioTab", "de/tobias/playpad/assets/view/option/profile/", PlayPadMain.getUiResourceBundle());

		if (playerActive) {
			audioTypeComboBox.setDisable(true);
			options.setDisable(true);
		}
	}

	@Override
	public void init() {
		audioViewController = new ArrayList<>();

		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// Audio Classes
		AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		audioTypeComboBox.getItems().addAll(audioHandlerRegistry.getTypes());

		// Listener for selection
		audioTypeComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			if (b != null && c != null)
				changeAudioSettings = true;
			showAudioSettings(c);
		});
		showAudioSettings(profileSettings.getAudioClass());
	}

	private void showAudioSettings(String classID) {
		if (audioViewController != null) {
			// Es gibt ein Settings View Controller der isChanged true ist
			if (audioViewController.stream().filter(c -> c.isChanged()).count() > 0) {
				changeAudioSettings = true;
			}
		}

		options.getChildren().clear();

		AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
		try {
			AudioHandlerFactory audio = audioHandlerRegistry.getFactory(classID);

			for (AudioCapability audioCapability : AudioCapability.getFeatures()) {
				options.getChildren().add(createCapabilityView(audio, audioCapability));
			}
		} catch (NoSuchComponentException e) {
			e.printStackTrace();
		}
	}

	private Parent createCapabilityView(AudioHandlerFactory audio, AudioCapability audioCapability) {
		HBox masterView = new HBox(14);
		VBox detailView = new VBox(14);

		Label nameLabel = new Label(Localization.getString(audioCapability.getName()));
		nameLabel.setAlignment(Pos.CENTER_RIGHT);
		nameLabel.setMinWidth(150);

		AudioHandlerViewController settingsViewController = null;

		Label availableLabel;
		if (audio.isFeatureAvaiable(audioCapability)) {
			availableLabel = new FontIcon(FontAwesomeType.CHECK);

			settingsViewController = audio.getAudioFeatureSettings(audioCapability);
		} else {
			availableLabel = new FontIcon(FontAwesomeType.TIMES);
		}

		detailView.getChildren().add(availableLabel);
		if (settingsViewController != null) {
			detailView.getChildren().add(settingsViewController.getParent());
			audioViewController.add(settingsViewController);
		}

		masterView.getChildren().addAll(nameLabel, detailView);
		return masterView;
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
		if (audioViewController.stream().filter(c -> c.isChanged()).count() > 0) {
			changeAudioSettings = true;
		}
		return changeAudioSettings;
	}

	@Override
	public Task<Void> getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				updateTitle(name());
				updateProgress(-1, -1);

				project.loadPadsContent();
				return null;
			}
		};
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
