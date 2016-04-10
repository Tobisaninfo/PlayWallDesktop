package de.tobias.playpad.action;

import java.util.List;

import de.tobias.playpad.settings.Profile;
import javafx.scene.control.TreeItem;

public abstract class ActionConnect {

	public abstract TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping);

	public abstract void initActionType(Mapping mapping, Profile profile);

	public abstract Action newInstance();

	public abstract ActionType geActionType();

	public abstract String getType();
}
