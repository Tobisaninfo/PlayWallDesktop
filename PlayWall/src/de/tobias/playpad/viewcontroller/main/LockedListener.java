package de.tobias.playpad.viewcontroller.main;

import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
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
