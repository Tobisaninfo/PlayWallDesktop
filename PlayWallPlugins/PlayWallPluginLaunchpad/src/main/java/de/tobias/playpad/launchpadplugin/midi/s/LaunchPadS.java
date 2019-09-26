package de.tobias.playpad.launchpadplugin.midi.s;

import de.thecodelabs.midi.feedback.Feedback;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.feedback.FeedbackValue;
import de.thecodelabs.midi.mapping.MidiKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.MidiCommand;
import de.thecodelabs.midi.midi.MidiCommandType;
import de.thecodelabs.midi.midi.feedback.MidiFeedbackTranscript;

import java.util.Optional;

public class LaunchPadS implements MidiFeedbackTranscript {

	public static final String NAME = "Launchpad S";
	public static final String NATIVE_NAME = "CoreMIDI4J - Launchpad S";

	@Override
	public void sendFeedback(MidiKey midiKey, FeedbackType feedbackType) {
		initDevice();

		MidiCommandType command = MidiCommandType.NOTE_ON;
		byte key = midiKey.getValue();

		if (key >= 104 && key <= 111) {
			command = MidiCommandType.CONTROL_CHANGE;
		}

		Feedback feedback = midiKey.getFeedbackForType(feedbackType);

		if (feedback == null) {
			return;
		}

		if (feedbackType != FeedbackType.WARNING) {
			Midi.getInstance().sendMessage(new MidiCommand(command, key, feedback.getValue()));
		} else {
			Midi.getInstance().sendMessage(new MidiCommand(command, key, (byte) (feedback.getValue() - 4)));
		}
	}

	@Override
	public FeedbackValue[] getFeedbackValues() {
		return LaunchPadSColor.values();
	}

	@Override
	public Optional<FeedbackValue> getFeedbackValueOfByte(byte b) {
		return Optional.ofNullable(LaunchPadSColor.valueOf(b));
	}

	public void initDevice() {
		// Flash Enable
		Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, (byte) 0, (byte) 40));
	}

	@Override
	public void clearFeedback() {
		Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, (byte) 0, (byte) 0));
	}
}
