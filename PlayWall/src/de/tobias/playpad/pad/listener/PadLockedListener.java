package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadLockedListener implements ChangeListener<Boolean> {

	private IPadViewController controller;

	public PadLockedListener(IPadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		controller.updateButtonDisable();
	}
}
