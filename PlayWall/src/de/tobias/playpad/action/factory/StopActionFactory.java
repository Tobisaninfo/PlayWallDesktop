package de.tobias.playpad.action.factory;

import de.tobias.playpad.action.*;
import de.tobias.playpad.action.actions.StopAction;
import de.tobias.playpad.profile.Profile;
import javafx.scene.control.TreeItem;

import java.util.List;

public class StopActionFactory extends ActionFactory {

	public StopActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(actions.get(0));
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		mapping.addActionIfNotContains(newInstance());
	}

	@Override
	public Action newInstance() {
		return new StopAction(getType());
	}

	@Override
	public ActionType geActionType() {
		return ActionType.CONTROL;
	}

}
