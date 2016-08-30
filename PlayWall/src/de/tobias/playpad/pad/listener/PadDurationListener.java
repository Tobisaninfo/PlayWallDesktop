package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadDurationListener implements ChangeListener<Duration> {

	private IPadViewController controller;

	public PadDurationListener(IPadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (controller != null) {
			controller.updateTimeLabel();
		}
	}
}
