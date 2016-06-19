package de.tobias.playpad.pad.listener;

import de.tobias.playpad.viewcontroller.pad.PadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadLockedListener implements ChangeListener<Boolean> {

	private PadViewController controller;

	public PadLockedListener(PadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		controller.updateButtonDisable();
	}
}
