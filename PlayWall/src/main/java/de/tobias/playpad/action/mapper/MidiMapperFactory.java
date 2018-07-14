package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mididevice.MidiDeviceImpl;
import de.tobias.playpad.midi.Midi;
import de.tobias.utils.util.Localization;

public class MidiMapperFactory extends MapperFactory implements MapperConnectFeedbackable {

	public MidiMapperFactory(String type) {
		super(type);
	}

	@Override
	public MapperViewController getQuickSettingsViewController(Mapper mapper) {
		return null;
	}

	@Override
	public Mapper createNewMapper() {
		return new MidiMapper(getType());
	}

	@Override
	public void initFeedbackType() {
		MidiDeviceImpl midiDeviceImpl = Midi.getInstance().getMidiDevice();
		if (midiDeviceImpl != null) {
			midiDeviceImpl.initDevice();
		}
	}

	@Override
	public void clearFeedbackType() {
		MidiDeviceImpl midiDeviceImpl = Midi.getInstance().getMidiDevice();
		if (midiDeviceImpl != null) {
			midiDeviceImpl.clearFeedback();
		}
	}

	// TODO Remove
	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Midi_Name);
	}
}
