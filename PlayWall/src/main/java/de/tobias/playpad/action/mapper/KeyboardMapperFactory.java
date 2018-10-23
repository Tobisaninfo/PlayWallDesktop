package de.tobias.playpad.action.mapper;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;

public class KeyboardMapperFactory extends MapperFactory {

	public KeyboardMapperFactory(String type) {
		super(type);
	}

	@Override
	public MapperViewController getQuickSettingsViewController(Mapper mapper) {
		return null;
	}

	@Override
	public Mapper createNewMapper() {
		return new KeyboardMapper(getType());
	}

	// TODO Remove
	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Keyboard_Name);
	}
}
