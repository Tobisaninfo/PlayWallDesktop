package de.tobias.playpad.actionsplugin.muteaction;

import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.settings.Profile;
import javafx.scene.control.TreeItem;

public class MuteActionFactory extends ActionFactory {

	public MuteActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionDisplayable> item = new TreeItem<>(actions.get(0));
		return item;
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		mapping.addActionIfNotContains(newInstance());
	}

	@Override
	public Action newInstance() {
		return new MuteAction(getType());
	}
	
	@Override
	public ActionType geActionType() {
		return ActionType.SETTINGS;
	}

}
