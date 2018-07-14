package de.tobias.playpad.viewcontroller.main.listener;

import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.nui.icon.FontAwesomeType;
import de.tobias.utils.nui.icon.FontIcon;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;

public class LockedListener implements ChangeListener<Boolean> {

	private IMainViewController mainViewController;

	private Label lockedLabel;

	public LockedListener(IMainViewController mainViewController) {
		this.mainViewController = mainViewController;

		lockedLabel = new Label();
		lockedLabel.setGraphic(new FontIcon(FontAwesomeType.LOCK));
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		if (mainViewController.getMenuToolbarController() != null) {
			MenuToolbarViewController menuToolbarViewController = mainViewController.getMenuToolbarController();
			menuToolbarViewController.setLocked(newValue);
			if (newValue) {
				menuToolbarViewController.addToolbarItem(lockedLabel);
			} else {
				menuToolbarViewController.removeToolbarItem(lockedLabel);
			}
		}
	}
}
