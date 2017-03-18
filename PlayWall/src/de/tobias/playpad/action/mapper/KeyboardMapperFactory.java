package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.utils.util.Localization;

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
