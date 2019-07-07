package de.tobias.playpad.plugin.media.action;

import de.tobias.playpad.action.*;
import de.tobias.playpad.profile.Profile;
import javafx.scene.control.TreeItem;

import java.util.List;

public class BlackActionFactory extends ActionProvider {

	static final String TYPE = "BLACK";

	public BlackActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(actions.get(0));
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		mapping.addActionIfNotContains(new BlackAction());
	}

	@Override
	public Action newInstance() {
		return new BlackAction();
	}

	@Override
	public ActionType geActionType() {
		return ActionType.SETTINGS;
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
