package de.tobias.playpad.action.connect;

import java.util.Collections;
import java.util.List;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.actions.NavigateAction;
import de.tobias.playpad.action.actions.NavigateAction.NavigationType;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.icon.MaterialDesignIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class NavigateActionConnect extends ActionConnect implements ActionDisplayable {

	public static final String TYPE = "NAVIGATE";

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
		mapping.addActionIfNotContains(new NavigateAction(NavigationType.PREVIOUS));
		mapping.addActionIfNotContains(new NavigateAction(NavigationType.NEXT));
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Action_Navigate_Name));
	}

	@Override
	public Node getGraphics() {
		return new FontIcon(MaterialDesignIcon.FONT_FILE, MaterialDesignIcon.NAVIGATION);
	}

	@Override
	public ContentViewController getSettingsViewController() {
		return null;
	}

	@Override
	public Action newInstance() {
		return new NavigateAction();
	}

	@Override
	public ActionType geActionType() {
		return ActionType.CONTROL;
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
