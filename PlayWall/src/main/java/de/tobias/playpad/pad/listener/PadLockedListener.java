package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadLockedListener implements ChangeListener<Boolean> {

	private AbstractPadViewController controller;

	public PadLockedListener(AbstractPadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		controller.updateButtonDisable();
	}
}
