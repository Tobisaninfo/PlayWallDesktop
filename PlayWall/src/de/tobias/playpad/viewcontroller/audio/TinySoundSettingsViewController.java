package de.tobias.playpad.viewcontroller.audio;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.audio.TinyAudioHandler;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class TinySoundSettingsViewController extends AudioHandlerViewController implements Callback<ListView<Info>, ListCell<Info>> {

	@FXML private ComboBox<Info> soundCardComboBox;
	@FXML private Button testButton;

	private boolean isChanged;

	private Clip clip;

	public TinySoundSettingsViewController() {
		super("tinySoundSettings", "de/tobias/playpad/assets/view/audio/", PlayPadMain.getUiResourceBundle());

		// Mixer (ohne Port)
		List<Info> infos = Arrays.stream(AudioSystem.getMixerInfo()).filter(info -> !info.getName().startsWith("Port"))
				.collect(Collectors.toList());

		testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));

		soundCardComboBox.getItems().setAll(infos);
		soundCardComboBox.setCellFactory(this);
		soundCardComboBox.setButtonCell(call(null));

		String name = (String) Profile.currentProfile().getProfileSettings().getAudioUserInfo().get(TinyAudioHandler.SOUND_CARD);
		for (Info info : AudioSystem.getMixerInfo()) {
			if (info.getName().equals(name)) {
				soundCardComboBox.getSelectionModel().select(info);
				break;
			}
		}

		// ersten Auswählen wenn keiner ausgewählt ist, damit keine Probleme auftreten da keiene Soundkarte ausgewäht ist
		if (soundCardComboBox.getSelectionModel().getSelectedItem() == null) {
			soundCardComboBox.getSelectionModel().selectFirst();
		}

		soundCardComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			if (clip != null) {
				clip.stop();
				clip = null;
				testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
			}

			isChanged = true;
			Profile.currentProfile().getProfileSettings().getAudioUserInfo().put(TinyAudioHandler.SOUND_CARD, c.getName());
		});
	}

	@FXML
	private void testButtonHandler(ActionEvent event) {
		try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(
					TinySoundSettingsViewController.class.getClassLoader().getResource("de/tobias/playpad/assets/files/Test-Sound.wav"));

			if (clip != null) {
				clip.stop();
				clip = null;
				testButton.setGraphic(new FontIcon(FontAwesomeType.PLAY));
			} else {

				clip = AudioSystem.getClip(soundCardComboBox.getValue());

				clip.open(audioIn);
				clip.start();
				Worker.runLater(() ->
				{
					while (true) {
						if (clip != null) {
							return;
						}
						if (!clip.isRunning()) {
							clip.drain();
							clip.close();
							break;
						}
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				testButton.setGraphic(new FontIcon(FontAwesomeType.STOP));
			}

		} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ListCell<Info> call(ListView<Info> param) {
		return new ListCell<Info>() {

			@Override
			protected void updateItem(Info item, boolean empty) {
				super.updateItem(item, empty);
				if (!empty) {
					setText(item.getName());
				} else {
					setText("");
				}
			}
		};
	}

	@Override
	public boolean isChanged() {
		return isChanged;
	}
}
