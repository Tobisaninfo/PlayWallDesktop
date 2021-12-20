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
import de.tobias.playpad.profile.Profile;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class LaunchPadMK2 implements MidiFeedbackTranscript, FeedbackColorSuggester {

	public static final String NAME = "Launchpad MK2";
	public static final String NATIVE_NAME = "CoreMIDI4J - Launchpad MK2";

	// Modern Colors mapped to the colors of the launchpad
	private static final String[] COLOR_MAPPING_FILES = {
			"launchpad_mk2_colorful.map",
			"launchpad_mk2_high.map",
			"launchpad_mk2_normal.map",
			"launchpad_mk2_low.map"
	};
	private static final String DEFAULT_COLOR_MAPPING = COLOR_MAPPING_FILES[0];
	private static final Map<String, Map<String, FeedbackColor>> midiColorMappings;

	static {
		midiColorMappings = new HashMap<>();
		try {
			for (String mappingFile : COLOR_MAPPING_FILES) {
				URL resource = LaunchPadMK2.class.getClassLoader().getResource(mappingFile);
				midiColorMappings.put(mappingFile, MapParser.load(resource, LaunchPadMK2Color.class));
			}
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
		final byte key = midiKey.getValue();

		if (feedbackType == FeedbackType.NONE) {
			if (key >= 104 && key <= 111) {
				Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, key, (byte) 0));
			} else {
				Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.NOTE_ON, key, (byte) 0));
			}
		}

		Feedback feedback = midiKey.getFeedbackForType(feedbackType);

		if (feedback == null) {
			return;
		}

		final byte value = feedback.getValue();

		if (key >= 104 && key <= 111) {
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.CONTROL_CHANGE, key, value));
		} else {
			if (feedbackType == FeedbackType.WARNING) {
				sendFeedback(midiKey, FeedbackType.DEFAULT);
			}
			Midi.getInstance().sendMessage(new MidiCommand(MidiCommandType.NOTE_ON, feedback.getChannel(), key, value));
		}
	}

	@Override
	public FeedbackValue[] getFeedbackValues() {
		String midiColorMapping = Profile.currentProfile().getProfileSettings().getMidiColorMapping();
		if (midiColorMapping == null || midiColorMapping.isEmpty()) {
			midiColorMapping = DEFAULT_COLOR_MAPPING;
		}
		final Map<String, FeedbackColor> colorMap = midiColorMappings.get(midiColorMapping);
		return colorMap.values().stream().sorted().distinct().toArray(FeedbackColor[]::new);
	}

	@Override
	public Optional<FeedbackValue> getFeedbackValueOfByte(byte b) {
		return Optional.ofNullable(LaunchPadMK2Color.valueOf(b));
	}

	/*
	FeedbackColorSuggester
	 */

	@Override
	public List<String> getMidiColorMappings() {
		return Arrays.asList(COLOR_MAPPING_FILES);
	}

	@Override
	public FeedbackColor suggest(Color color) {
		final String midiColorMapping = Optional.ofNullable(Profile.currentProfile().getProfileSettings().getMidiColorMapping())
				.orElse(DEFAULT_COLOR_MAPPING);
		final Map<String, FeedbackColor> colorMap = midiColorMappings.get(midiColorMapping);

		if (colorMap.containsKey(color.toString())) {
			return colorMap.get(color.toString());
		}
		return null;
	}
}
