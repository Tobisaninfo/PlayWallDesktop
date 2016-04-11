package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Strings;
import de.tobias.utils.util.Localization;

public class KeyboardMapperConnect extends MapperConnect {

	public static final String TYPE = "KEYBOARD";

	@Override
	public MapperViewController getQuickSettingsViewController(Mapper mapper) {
		return null;
	}

	@Override
	public Mapper createNewMapper() {
		return new KeyboardMapper();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Mapper_Keyboard_Name);
	}
}
