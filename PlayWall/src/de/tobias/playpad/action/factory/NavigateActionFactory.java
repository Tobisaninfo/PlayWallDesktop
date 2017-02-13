package de.tobias.playpad.action.factory;

import de.tobias.playpad.action.*;
import de.tobias.playpad.action.actions.NavigateAction;
import de.tobias.playpad.action.actions.NavigateAction.NavigationType;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.nui.NVC;
import javafx.scene.control.TreeItem;

import java.util.Collections;
import java.util.List;

public class NavigateActionFactory extends ActionFactory implements ActionDisplayable {

	public NavigateActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>(this);

		Collections.sort(actions, (o1, o2) ->
		{
			if (o1 instanceof NavigateAction && o2 instanceof NavigateAction) {
				NavigateAction c1 = (NavigateAction) o1;
				NavigateAction c2 = (NavigateAction) o2;
				return Long.compare(c1.getAction().ordinal(), c2.getAction().ordinal());
			} else {
				return -1;
			}
		});

		for (Action action : actions) {
			TreeItem<ActionDisplayable> actionItem = new TreeItem<>(action);
			rootItem.getChildren().add(actionItem);
		}
		return rootItem;
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		mapping.addActionIfNotContains(new NavigateAction(getType(), NavigationType.PREVIOUS));
		mapping.addActionIfNotContains(new NavigateAction(getType(), NavigationType.NEXT));
	}

	@Override
	public NVC getSettingsViewController() {
		return null;
	}

	@Override
	public Action newInstance() {
		return new NavigateAction(getType());
	}

	@Override
	public ActionType geActionType() {
		return ActionType.CONTROL;
	}

}
