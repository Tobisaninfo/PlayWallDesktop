package de.tobias.playpad.layout.touch;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.listener.IPadPositionListener;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

public class TouchPadViewController implements IPadViewControllerV2 {

	@Override
	public Pad getPad() {
		return null;
	}

	@Override
	public IPadViewV2 getView() {
		return null;
	}

	@Override
	public void setupPad(Pad pad) {
		
	}

	@Override
	public void removePad() {
		
	}

	@Override
	public void updateTimeLabel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateButtonDisable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IPadPositionListener getPadPositionListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeListener<Duration> getPadDurationListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
