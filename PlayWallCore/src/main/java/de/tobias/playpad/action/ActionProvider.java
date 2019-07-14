package de.tobias.playpad.action;

import de.thecodelabs.midi.Mapping;
import de.tobias.playpad.registry.Component;

public abstract class ActionProvider extends Component {

	public ActionProvider(String type) {
		super(type);
	}

	public abstract String getType();

	public abstract void createDefaultActions(Mapping mapping);
}
