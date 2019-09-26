package de.tobias.playpad.audio.mac.settings;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.audio.mac.AVAudioPlayerBridge;
import de.tobias.playpad.audio.mac.AudioDevice;
import de.tobias.playpad.audio.mac.NativeAudioMacHandler;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import java.nio.file.Path;

public class NativeAudioMacSettingsViewController extends AudioHandlerViewController implements AVAudioPlayerBridge.NativeAudioDelegate {

	private static final String TEST_SOUND_WAV = "Test-Sound.wav";

	@FXML
	private ComboBox<AudioDevice> soundCardComboBox;
	@FXML
	private Button testButton;

	private AVAudioPlayerBridge audioPlayer = new AVAudioPlayerBridge();

	private boolean isChanged;

	public NativeAudioMacSettingsViewController() {
		super("namacSettings", "mac", null);
		final ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));

		// load test sound
		final App app = ApplicationUtils.getApplication();
		Path file = app.getPath(PathType.RESOURCES, TEST_SOUND_WAV);
		app.getClasspathResource(TEST_SOUND_WAV).copy(PathType.RESOURCES, TEST_SOUND_WAV);

		audioPlayer.setDelegate(this);
		audioPlayer.load(file.toString());

		soundCardComboBox.getItems().setAll(AVAudioPlayerBridge.getAudioDevices());

		String name = (String) profileSettings.getAudioUserInfo().get(NativeAudioMacHandler.SOUND_CARD);
		for (AudioDevice device : AVAudioPlayerBridge.getAudioDevices()) {
			if (device.getName().equals(name)) {
				soundCardComboBox.getSelectionModel().select(device);
				break;
			}
		}

		// ersten Auswählen wenn keiner ausgewählt ist, damit keine Probleme
		// auftreten da keiene Soundkarte ausgewäht ist
		if (soundCardComboBox.getSelectionModel().getSelectedItem() == null) {
			soundCardComboBox.getSelectionModel().selectFirst();
		}

		soundCardComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> {
			if (audioPlayer != null) {
				audioPlayer.stop();
				testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
			}

			isChanged = true;
			profileSettings.getAudioUserInfo().put(NativeAudioMacHandler.SOUND_CARD, c.getName());
		});
	}

	@Override
	public void onClose() {
		if (audioPlayer != null) {
			audioPlayer.stop();
			audioPlayer = null;
		}
	}

	@FXML
	private void testButtonHandler(ActionEvent event) {
		if (audioPlayer == null) {
			return;
		}

		if (audioPlayer.isPlaying()) {
			audioPlayer.stop();
			testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
		} else {
			audioPlayer.setCurrentAudioDevice(soundCardComboBox.getValue().getId());
			audioPlayer.play();
			testButton.setGraphic(new FontIcon(FontAwesomeType.STOP));
		}
	}

	@Override
	public boolean isChanged() {
		return isChanged;
	}

	/*
	AVAudioPlayerBridge.NativeAudioDelegate
	 */

	@Override
	public void onFinish(AVAudioPlayerBridge bridge) {
		Platform.runLater(() -> testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY)));
	}

	@Override
	public void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right) {

	}

	@Override
	public void onPositionChanged(AVAudioPlayerBridge bridge, double position) {

	}
}
