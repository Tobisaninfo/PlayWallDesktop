package de.tobias.playpad.viewcontroller.main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class VolumeChangeListener implements ChangeListener<Number> {

	private IMainViewController mainViewController;

	public VolumeChangeListener(IMainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		mainViewController.setGlobalVolume(newValue.doubleValue());
	}
}
