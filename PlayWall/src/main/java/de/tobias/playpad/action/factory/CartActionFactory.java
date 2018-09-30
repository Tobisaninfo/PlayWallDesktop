package de.tobias.playpad.action.factory;

import de.tobias.playpad.action.*;
import de.tobias.playpad.action.actions.cart.CartAction;
import de.tobias.playpad.action.actions.cart.CartAction.CartActionMode;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.actions.CartActionTypeViewController;
import de.tobias.utils.ui.NVC;
import javafx.scene.control.TreeItem;

import java.util.List;

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
				CartAction action = new CartAction(getType(), x, y, CartActionMode.PLAY_STOP);
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
