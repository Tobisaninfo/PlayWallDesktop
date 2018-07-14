package de.tobias.playpad.midi.device;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mididevice.MidiDeviceImpl;
import de.tobias.playpad.profile.Profile;
import javafx.application.Platform;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;

public class PD12 extends MidiDeviceImpl {

	public static final String NAME = "PD 12";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean supportFeedback() {
		return false;
	}

	@Override
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

	@Override
	public void handleFeedback(FeedbackMessage type, int key, Feedback feedback) {}

	@Override
	public void initDevice() {}

	@Override
	public void clearFeedback() {}

	@Override
	public DisplayableFeedbackColor getColor(int id) {
		return null;
	}

	@Override
	public DisplayableFeedbackColor[] getColors() {
		return null;
	}
}
