package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadDurationListener implements ChangeListener<Duration> {

	private IPadViewControllerV2 controller;

	public PadDurationListener(IPadViewControllerV2 controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (controller != null) {
			controller.updateTimeLabel();
		}
	}
}
