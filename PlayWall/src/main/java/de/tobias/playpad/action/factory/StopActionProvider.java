package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.StopActionTypeSettingsEntry;
import javafx.scene.control.TreeItem;

import java.util.List;

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

	/*

	 */

	@Override
	public ActionType getActionType() {
		return ActionType.CONTROL;
	}

	@Override
	public TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(new StopActionTypeSettingsEntry(actions.stream().findFirst().orElse(null)));
	}
}
