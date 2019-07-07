package de.tobias.playpad.viewcontroller.option.profile;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectLoadDialog;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AudioTabViewController extends ProfileSettingsTabViewController implements IProfileReloadTask {

	// Audio
	@FXML
	private ComboBox<String> audioTypeComboBox;
	@FXML
	private VBox options;

	private List<AudioHandlerViewController> audioViewController;
	private boolean changeAudioSettings;

	public AudioTabViewController(boolean playerActive) {
		load("view/option/profile", "AudioTab", PlayPadMain.getUiResourceBundle());

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
		AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistries().getAudioHandlers();
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
			if (audioViewController.stream().anyMatch(AudioHandlerViewController::isChanged)) {
				changeAudioSettings = true;
			}
		}

		options.getChildren().clear();

		AudioRegistry audioHandlerRegistry = PlayPadPlugin.getRegistries().getAudioHandlers();
		AudioHandlerFactory audio = audioHandlerRegistry.getFactory(classID);

		for (AudioCapability audioCapability : AudioCapability.getFeatures()) {
			options.getChildren().add(createCapabilityView(audio, audioCapability));
		}
	}

	/**
	 * Create Settings View for one AudioCapability
	 *
	 * @param audio           current audio impl
	 * @param audioCapability audioCapability
	 * @return ui element
	 */
	private Parent createCapabilityView(AudioHandlerFactory audio, AudioCapability audioCapability) {
		HBox masterView = new HBox(14);
		VBox detailView = new VBox(14);

		Label nameLabel = new Label(audioCapability.getName());
		nameLabel.setAlignment(Pos.CENTER_RIGHT);
		nameLabel.setMinWidth(150);

		AudioHandlerViewController settingsViewController = null;

		Label availableLabel;
		if (audio.isFeatureAvailable(audioCapability)) {
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

		if (audioViewController != null) {
			audioViewController.forEach(AudioHandlerViewController::onClose);
		}
	}

	@Override
	public boolean needReload() {
		if (audioViewController.stream().filter(AudioHandlerViewController::isChanged).count() > 0) {
			changeAudioSettings = true;
		}
		return changeAudioSettings;
	}

	@Override
	public Runnable getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		ProjectLoadDialog listener = new ProjectLoadDialog();

		return () -> {
			Collection<Pad> pads = project.getPads();
			List<Pad> filteredPads = pads.parallelStream().filter(pad -> pad.getStatus() != PadStatus.EMPTY).collect(Collectors.toList());
			long padContentCount = filteredPads.size();

			listener.totalMedia((int) padContentCount);

			for (Pad pad : filteredPads) {
				listener.readMedia(pad.getName());
				pad.loadContent();
			}
			listener.finish();
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
