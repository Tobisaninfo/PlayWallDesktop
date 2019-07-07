package de.tobias.playpad.launchpadplugin.midi.mk2;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mididevice.DeviceColorAssociatorConnector;
import de.tobias.playpad.action.mididevice.MidiDeviceImpl;
import de.tobias.playpad.launchpadplugin.impl.MapParser;
import de.tobias.playpad.midi.Midi;
import javafx.scene.paint.Color;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;
import java.net.URL;
import java.util.Map;

public class LaunchPadMK2 extends MidiDeviceImpl implements DeviceColorAssociatorConnector {

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
	public String getName() {
		return NAME;
	}

	@Override
	public boolean supportFeedback() {
		return true;
	}

	@Override
	public void initDevice() {
		// nothing to do
	}

	@Override
	public void handleFeedback(FeedbackMessage type, int key, Feedback feedback) {
		if (type != FeedbackMessage.WARNING) {
			try {
				int value = feedback.getValueForFeedbackMessage(type);
				if (key >= 104 && key <= 111) {
					Midi.getInstance().sendMessage(176, key, value);
				} else {
					Midi.getInstance().sendMessage(144, key, value);
				}
			} catch (MidiUnavailableException | InvalidMidiDataException e) {
				Logger.error(e);
			}
		} else {
			try {
				Midi.getInstance().sendMessage(145, key, feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));
			} catch (MidiUnavailableException | InvalidMidiDataException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public void clearFeedback() {
		final int maxMainKeyNumber = 89;

		for (int i = 11; i <= maxMainKeyNumber; i++) {
			// Node_On = 144
			try {
				Midi.getInstance().sendMessage(ShortMessage.NOTE_ON, i, 0);
			} catch (MidiUnavailableException | InvalidMidiDataException e) {
				Logger.error(e);
			}
		}

		// Obere Reihe an Tasten
		final int liveKeyMin = 104;
		final int liveKeyMax = 111;

		for (int i = liveKeyMin; i <= liveKeyMax; i++) {
			// Control_Change = 176
			try {
				Midi.getInstance().sendMessage(ShortMessage.CONTROL_CHANGE, i, 0);
			} catch (MidiUnavailableException | InvalidMidiDataException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public DisplayableFeedbackColor getColor(int id) {
		return LaunchPadMK2Color.valueOf(id);
	}

	@Override
	public DisplayableFeedbackColor[] getColors() {
		return LaunchPadMK2Color.values();
	}

	@Override
	public DisplayableFeedbackColor getDefaultEventColor() {
		return LaunchPadMK2Color.C5;
	}

	@Override
	public DisplayableFeedbackColor getDefaultStandardColor() {
		return LaunchPadMK2Color.C36;
	}

	@Override
	public DisplayableFeedbackColor getPreferColorMapping(Color color) {
		if (mapProperties.containsKey(color.toString())) {
			String nameOfConst = mapProperties.get(color.toString());
			return LaunchPadMK2Color.valueOf(nameOfConst);
		}
		return null;
	}
}
