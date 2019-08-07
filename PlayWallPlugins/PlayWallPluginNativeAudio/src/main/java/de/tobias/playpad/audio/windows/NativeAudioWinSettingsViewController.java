package de.tobias.playpad.audio.windows;

import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import nativeaudio.NativeAudio;

import java.nio.file.Path;

public class NativeAudioWinSettingsViewController extends AudioHandlerViewController {

	private static final String TEST_SOUND_WAV = "Test-Sound.wav";

	@FXML
	private ComboBox<String> soundCardComboBox;
	@FXML
	private Button testButton;

	private boolean isChanged;

	private NativeAudio audioPlayer;

	NativeAudioWinSettingsViewController() {
		super("nawinSettings", "win", null);
		final ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));

		soundCardComboBox.getItems().setAll(NativeAudio.getDevices());

		String name = (String) profileSettings.getAudioUserInfo().get(NativeAudioWinHandler.SOUND_CARD);
		for (String info : NativeAudio.getDevices()) {
			if (info.equals(name)) {
				soundCardComboBox.getSelectionModel().select(info);
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
				audioPlayer = null;
				testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
			}

			isChanged = true;
			profileSettings.getAudioUserInfo().put(NativeAudioWinHandler.SOUND_CARD, c);
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
			final App app = ApplicationUtils.getApplication();
			Path file = app.getPath(PathType.RESOURCES, TEST_SOUND_WAV);
			app.getClasspathResource(TEST_SOUND_WAV).copy(PathType.RESOURCES, TEST_SOUND_WAV);

			audioPlayer = new NativeAudio();
			audioPlayer.load(file.toString());
			audioPlayer.setDevice(soundCardComboBox.getValue());
		}
		if (audioPlayer.isPlaying()) {
			audioPlayer.stop();
			audioPlayer = null;
			testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
		} else {
			audioPlayer.play();
			Worker.runLater(() -> {
				while (audioPlayer != null && audioPlayer.isPlaying()) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (audioPlayer != null)
					audioPlayer.stop();
				Platform.runLater(() -> testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY)));
			});
			testButton.setGraphic(new FontIcon(FontAwesomeType.STOP));
		}
	}

	@Override
	public boolean isChanged() {
		return isChanged;
	}
}
