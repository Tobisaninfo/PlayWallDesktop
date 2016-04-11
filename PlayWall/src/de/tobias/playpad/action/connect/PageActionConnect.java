package de.tobias.playpad.action.connect;

import java.util.Collections;
import java.util.List;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.actions.PageAction;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class PageActionConnect extends ActionConnect implements ActionDisplayable {

	public static final String TYPE = "PAGE";

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
		ProfileSettings profileSettings = profile.getProfileSettings();
		int pages = profileSettings.getPageCount();
		for (int i = 0; i < pages; i++) {
			PageAction action = new PageAction(i);
			mapping.addActionIfNotContains(action);
		}
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Action_Page_Name));
	}

	@Override
	public Node getGraphics() {
		return new FontIcon(FontAwesomeType.FILE_TEXT);
	}

	@Override
	public ContentViewController getSettingsViewController() {
		return null;
	}

	@Override
	public Action newInstance() {
		return new PageAction();
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
