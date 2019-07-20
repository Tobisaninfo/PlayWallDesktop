package de.tobias.playpad.launchpadplugin.midi.mk2;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.feedback.Feedback;
import de.thecodelabs.midi.feedback.FeedbackColor;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.feedback.FeedbackValue;
import de.thecodelabs.midi.mapping.MidiKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.MidiCommand;
import de.thecodelabs.midi.midi.MidiCommandType;
import de.thecodelabs.midi.midi.feedback.MidiFeedbackTranscript;
import de.tobias.playpad.action.feedback.FeedbackColorSuggester;
import de.tobias.playpad.launchpadplugin.impl.MapParser;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Map;

public class LaunchPadMK2 implements MidiFeedbackTranscript, FeedbackColorSuggester {

	public static final String NAME = "Launchpad MK2";

	// Modern Colors mapped to the colors of the launchpad
	private static Map<String, String> mapProperties;

	static {
		try {
			URL resource = LaunchPadMK2.class.getClassLoader().getResource("launchpad_mk2.map");
			mapProperties = MapParser.load(resource);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@Override
	public void clearFeedback() {
		final int maxMainKeyNumber = 89;

		for (byte i = 11; i <= maxMainKeyNumber; i++) {
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.NOTE_ON, i, (byte) 0));
		}

		// Obere Reihe an Tasten
		final int liveKeyMin = 104;
		final int liveKeyMax = 111;

		for (byte i = liveKeyMin; i <= liveKeyMax; i++) {
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, i, (byte) 0));
		}
	}

	@Override
	public void sendFeedback(MidiKey midiKey, FeedbackType feedbackType) {
		Feedback feedback = midiKey.getFeedbackForType(feedbackType);

		if (feedback == null) {
			return;
		}

		final byte key = midiKey.getValue();
		final byte value = feedback.getValue();

		if (key >= 104 && key <= 111) {
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, key, value));
		} else {
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.NOTE_ON, feedback.getChannel(), key, value));
		}
	}

	@Override
	public FeedbackValue[] getFeedbackValues() {
		return LaunchPadMK2Color.values();
	}

	@Override
	public FeedbackColor suggest(Color color) {
		if (mapProperties.containsKey(color.toString())) {
			String nameOfConst = mapProperties.get(color.toString());
			return LaunchPadMK2Color.valueOf(nameOfConst);
		}
		return null;
	}
}
