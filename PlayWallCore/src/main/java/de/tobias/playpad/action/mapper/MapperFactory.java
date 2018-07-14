package de.tobias.playpad.action.mapper;

import de.tobias.playpad.registry.Component;

public abstract class MapperFactory extends Component {

	public MapperFactory(String type) {
		super(type);
	}

	public abstract Mapper createNewMapper();

	public abstract MapperViewController getQuickSettingsViewController(Mapper mapper);
}
