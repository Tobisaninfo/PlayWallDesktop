package de.tobias.playpad.action.mapper;

public abstract class MapperConnect {

	public abstract Mapper createNewMapper();

	public abstract MapperViewController getQuickSettingsViewController(Mapper mapper);

	public abstract String getType();
}
