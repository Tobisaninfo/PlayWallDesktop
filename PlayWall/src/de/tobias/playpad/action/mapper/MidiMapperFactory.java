package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.Midi;
import de.tobias.utils.ui.icon.FontIconType;
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
		Midi.getInstance().getMidiDevice().ifPresent(Device::initDevice);
	}

	@Override
	public void clearFeedbackType() {
		Midi.getInstance().getMidiDevice().ifPresent(Device::clearFeedback);
	}

	// TODO Remove
	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Midi_Name);
	}
}
