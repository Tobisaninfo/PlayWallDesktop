package de.tobias.playpad.mediaplugin.blindaction;

import de.tobias.playpad.action.*;
import de.tobias.playpad.profile.Profile;
import javafx.scene.control.TreeItem;

import java.util.List;

public class BlindActionFactory extends ActionFactory {

	static final String TYPE = "BLIND";

	public BlindActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(actions.get(0));
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		mapping.addActionIfNotContains(new BlindAction());
	}

	@Override
	public Action newInstance() {
		return new BlindAction();
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