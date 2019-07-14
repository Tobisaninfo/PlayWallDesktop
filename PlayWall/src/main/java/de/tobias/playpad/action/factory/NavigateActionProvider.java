package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.actions.NavigateAction;
import de.tobias.playpad.action.actions.NavigateAction.NavigationType;

import static de.tobias.playpad.action.actions.NavigateAction.TYPE;

public class NavigateActionProvider extends ActionProvider {

	public NavigateActionProvider() {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		mapping.addUniqueAction(newInstance(NavigationType.PREVIOUS));
		mapping.addUniqueAction(newInstance(NavigationType.NEXT));
	}

	private Action newInstance(NavigationType navigationType) {
		Action action = new Action(getType());
		action.addPayloadEntry(NavigateAction.PAYLOAD_TYPE, navigationType.name());
		return action;
	}

}
