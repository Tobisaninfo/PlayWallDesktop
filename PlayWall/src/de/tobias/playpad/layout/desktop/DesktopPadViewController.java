package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DesktopPadViewController implements IPadViewControllerV2, EventHandler<ActionEvent> {

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
	public void handle(ActionEvent event) {
		
	}

}
