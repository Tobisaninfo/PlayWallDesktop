package de.tobias.playpad.action.factory;

import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.action.ActionDisplayable;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.cartaction.CartAction.ControlMode;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.actions.CartActionTypeViewController;
import de.tobias.utils.nui.NVC;
import javafx.scene.control.TreeItem;

public class CartActionFactory extends ActionFactory implements ActionDisplayable {

	public CartActionFactory(String type) {
		super(type);
	}

	@Override
	public TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>(this);
		return rootItem;
	}

	@Override
	public void initActionType(Mapping mapping, Profile profile) {
		for (int x = 0; x < ProjectSettings.MAX_COLUMNS; x++) {
			for (int y = 0; y < ProjectSettings.MAX_ROWS; y++) {
				CartAction action = new CartAction(getType(), x, y, ControlMode.PLAY_STOP);
				mapping.addActionIfNotContains(action);
			}
		}
	}

	// Settings View (Übersicht mit den Buttons). Die Buttons rufen dann die jeweilige CartAction auf. Da muss dann auch die MapperView
	// manuell gesetzt werden.
	@Override
	public NVC getActionSettingsViewController(Mapping mapping, IMappingTabViewController controller) {
		return new CartActionTypeViewController(mapping, controller);
	}

	@Override
	public Action newInstance() {
		return new CartAction(getType());
	}

	@Override
	public ActionType geActionType() {
		return ActionType.CONTROL;
	}

}
