package de.tobias.playpad.midi;

import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.MidiCommand;
import de.thecodelabs.midi.midi.MidiCommandType;
import de.thecodelabs.midi.midi.MidiListener;
import de.tobias.playpad.profile.Profile;
import javafx.application.Platform;

public class PD12 implements MidiListener {

	@Override
	public void onMidiMessage(MidiCommand midiCommand) {
		if (!Midi.getInstance().getDevice().getMidiDeviceInfo().getName().contains("PD 12")) {
			return;
		}

		if (midiCommand.getMidiCommand() == MidiCommandType.SYSTEM_EXCLUSIVE) {
			final byte[] payload = midiCommand.getPayload();
			if (payload[0] == 127 && payload[1] == 127
					&& payload[2] == 4 && payload[3] == 1 && payload[4] == 0
					&& payload[6] == -9) {
				int volume = payload[5];
				double calculatedVolume = volume / 127.0;
				Platform.runLater(() -> Profile.currentProfile().getProfileSettings().setVolume(calculatedVolume));
			}
		}
	}
}
