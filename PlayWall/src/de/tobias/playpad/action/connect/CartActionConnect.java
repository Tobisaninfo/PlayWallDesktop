package de.tobias.playpad.action.connect;

import java.util.List;

import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.cartaction.CartAction.ControlMode;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.actions.CartActionsViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class CartActionConnect extends ActionConnect implements ActionDisplayable {

	public static final String TYPE = "CART";

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>(this);
		return rootItem;
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		for (int x = 0; x < ProjectSettings.MAX_COLUMNS; x++) {
			for (int y = 0; y < ProjectSettings.MAX_ROWS; y++) {
				CartAction action = new CartAction(x, y, ControlMode.PLAY_STOP);
				mapping.addActionIfNotContains(action);
			}

		}
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Action_Cart_Name));
	}

	@Override
	public Node getGraphics() {
		return new FontIcon(FontAwesomeType.TH);
	}

	// Settings View (Übersicht mit den Buttons). Die Buttons rufen dann die jeweilige CartAction auf. Da muss dann auch die MapperView
	// manuell gesetzt werden.
	@Override
	public ContentViewController getActionSettingsViewController(Mapping mapping, IMappingTabViewController controller) {
		return new CartActionsViewController(mapping, controller);
	}

	@Override
	public Action newInstance() {
		return new CartAction();
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
