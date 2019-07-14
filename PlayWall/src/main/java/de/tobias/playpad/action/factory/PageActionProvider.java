package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.actions.PageAction;
import de.tobias.playpad.project.ProjectSettings;

import static de.tobias.playpad.action.actions.PageAction.TYPE;

public class PageActionProvider extends ActionProvider {

	public PageActionProvider() {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		for (int i = 0; i < ProjectSettings.MAX_PAGES; i++) {
			Action action = newInstance(i);
			mapping.addUniqueAction(action);
		}
	}

	private Action newInstance(int i) {
		Action action = new Action(getType());
		action.addPayloadEntry(PageAction.PAYLOAD_PAGE, String.valueOf(i));
		return action;
	}
}
