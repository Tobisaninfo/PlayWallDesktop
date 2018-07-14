package de.tobias.playpad.launchpadplugin.midi.device.s;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mididevice.DeviceColorAssociatorConnector;
import de.tobias.playpad.action.mididevice.MidiDeviceImpl;
import de.tobias.playpad.midi.Midi;
import javafx.scene.paint.Color;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class LaunchPadS extends MidiDeviceImpl implements DeviceColorAssociatorConnector {

	public static final String NAME = "Launchpad S";

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
		// Flash Enable
		try {
			Midi.getInstance().sendMessage(176, 0, 40);
		} catch (MidiUnavailableException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleFeedback(FeedbackMessage type, int key, Feedback feedback) {
		initDevice();

		int command = 144;

		if (key >= 104 && key <= 111) {
			command = 176;
		}

		try {
			if (type != FeedbackMessage.WARNING) {
				int value = feedback.getValueForFeedbackMessage(type);
				Midi.getInstance().sendMessage(command, key, value);
			} else {
				int midiVelocity = feedback.getValueForFeedbackMessage(FeedbackMessage.EVENT) - 4;
				Midi.getInstance().sendMessage(command, key, midiVelocity);
			}
		} catch (MidiUnavailableException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void clearFeedback() {
		try {
			Midi.getInstance().sendMessage(176, 0, 0);
		} catch (MidiUnavailableException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DisplayableFeedbackColor getColor(int id) {
		return LaunchPadSColor.valueOf(id);
	}

	@Override
	public DisplayableFeedbackColor[] getColors() {
		return LaunchPadSColor.values();
	}

	@Override
	public DisplayableFeedbackColor getDefaultEventColor() {
		return LaunchPadSColor.RED;
	}

	@Override
	public DisplayableFeedbackColor getDefaultStandardColor() {
		return LaunchPadSColor.GREEN;
	}

	@Override
	public DisplayableFeedbackColor map(Color color) {
		return null;
	}
}
