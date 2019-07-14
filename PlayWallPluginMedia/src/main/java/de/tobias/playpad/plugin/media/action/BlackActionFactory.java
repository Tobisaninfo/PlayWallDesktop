package de.tobias.playpad.plugin.media.action;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.tobias.playpad.action.ActionProvider;

import static de.tobias.playpad.plugin.media.action.BlackAction.TYPE;

public class BlackActionFactory extends ActionProvider {

	public BlackActionFactory() {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		mapping.addUniqueAction(new Action(getType()));
	}
}
