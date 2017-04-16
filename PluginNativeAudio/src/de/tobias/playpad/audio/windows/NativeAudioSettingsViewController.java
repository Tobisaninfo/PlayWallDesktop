package de.tobias.playpad.audio.windows;

import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.IOUtils;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import nativeaudio.NativeAudio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class NativeAudioSettingsViewController extends AudioHandlerViewController {

	@FXML private ComboBox<String> soundCardComboBox;
	@FXML private Button testButton;

	private boolean isChanged;

	private NativeAudio audioPlayer;

	public NativeAudioSettingsViewController() {
		super("nawinSettings", "de/tobias/playpad/nawin/assets/", null);

		testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));

		soundCardComboBox.getItems().setAll(NativeAudio.getDevices());

		String name = (String) Profile.currentProfile().getProfileSettings().getAudioUserInfo()
				.get(NativeAudioWinHandler.SOUND_CARD);
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
			Profile.currentProfile().getProfileSettings().getAudioUserInfo().put(NativeAudioWinHandler.SOUND_CARD, c);
		});
	}

	@FXML
	private void testButtonHandler(ActionEvent event) {
		Path file = ApplicationUtils.getApplication().getPath(PathType.RESOURCES, "Test-Sound.wav");
		if (Files.notExists(file)) {
			InputStream iStr = getClass().getClassLoader()
					.getResourceAsStream("de/tobias/playpad/nawin/assets/Test-Sound.wav");
			try {
				Files.createDirectories(file.getParent());
				IOUtils.copy(iStr, file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (audioPlayer == null) {
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
