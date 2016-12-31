package de.tobias.playpad.action.factory;

import java.util.Collections;
import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.actions.PageAction;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class PageActionFactory extends ActionFactory implements ActionDisplayable {

	public PageActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>(this);

		Collections.sort(actions, (o1, o2) ->
		{
			if (o1 instanceof PageAction && o2 instanceof PageAction) {
				PageAction c1 = (PageAction) o1;
				PageAction c2 = (PageAction) o2;
				return Long.compare(c1.getPage(), c2.getPage());
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
		for (int i = 0; i < ProjectSettings.MAX_PAGES; i++) {
			PageAction action = new PageAction(getType(), i);
			mapping.addActionIfNotContains(action);
		}
	}

	@Override
	public ContentViewController getSettingsViewController() {
		return null;
	}

	@Override
	public Action newInstance() {
		return new PageAction(getType());
	}

	@Override
	public ActionType geActionType() {
		return ActionType.CONTROL;
	}

}
