package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.tobias.playpad.action.ActionProvider;

import static de.tobias.playpad.action.actions.StopAction.TYPE;

public class StopActionProvider extends ActionProvider {

	public StopActionProvider() {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		mapping.addUniqueAction(newInstance());
	}

	private Action newInstance() {
		return new Action(getType());
	}
}
