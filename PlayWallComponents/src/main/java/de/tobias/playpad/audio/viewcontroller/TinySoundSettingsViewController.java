package de.tobias.playpad.audio.viewcontroller;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.audio.TinyAudioHandler;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.Info;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class TinySoundSettingsViewController extends AudioHandlerViewController implements Callback<ListView<Info>, ListCell<Info>> {

	@FXML
	private ComboBox<Info> soundCardComboBox;
	@FXML
	private Button testButton;

	private boolean isChanged;

	private Clip clip;

	public TinySoundSettingsViewController() {
		super("TinySoundSettings", "view/audio", Localization.getBundle());

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

	@Override
	public void onClose() {
		if (clip != null) {
			clip.stop();
			clip = null;
		}
	}

	@FXML
	private void testButtonHandler(ActionEvent event) {
		try {
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(
					TinySoundSettingsViewController.class.getClassLoader().getResource("sfx/Test-Sound.wav"));

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
							Logger.error(e);
						}
					}
				});
				testButton.setGraphic(new FontIcon(FontAwesomeType.STOP));
			}

		} catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
			Logger.error(e);
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
