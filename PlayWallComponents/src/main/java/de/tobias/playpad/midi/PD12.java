package de.tobias.playpad.midi;

import de.tobias.playpad.profile.Profile;
import javafx.application.Platform;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;

// TODO Handle PD12 Slider
public class PD12  {

	public static final String NAME = "PD 12";

	public void onMidiMessage(MidiMessage message) {
		if (message instanceof SysexMessage) {
			if (message.getMessage().length == 8) {
				if (message.getMessage()[0] == -16 && message.getMessage()[1] == 127 && message.getMessage()[2] == 127
						&& message.getMessage()[3] == 4 && message.getMessage()[4] == 1 && message.getMessage()[5] == 0
						&& message.getMessage()[7] == -9) {
					int volume = message.getMessage()[6];
					double volume_ = volume / 127.0;
					Platform.runLater(() -> Profile.currentProfile().getProfileSettings().setVolume(volume_));
				}
			}
		}
	}
}
