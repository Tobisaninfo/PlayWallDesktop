package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.utils.util.Localization;

public class MidiMapperConnect extends MapperConnect implements MapperConnectFeedbackable {

	public static final String TYPE = "MIDI";

	@Override
	public MapperViewController getQuickSettingsViewController(Mapper mapper) {
		return null;
	}

	@Override
	public Mapper createNewMapper() {
		return new MidiMapper();
	}

	@Override
	public void initFeedbackType() {
		Midi.getInstance().getMidiDevice().ifPresent(device -> device.initFeedback());
	}

	@Override
	public void clearFeedbackType() {
		Midi.getInstance().getMidiDevice().ifPresent(device -> device.clearFeedback());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Midi_Name);
	}
}
