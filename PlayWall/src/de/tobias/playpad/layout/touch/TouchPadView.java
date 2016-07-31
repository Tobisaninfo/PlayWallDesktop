package de.tobias.playpad.layout.touch;

import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class TouchPadView implements IPadViewV2 {

	@Override
	public IPadContentView getContentView() {
		return null;
	}

	@Override
	public void setContentView(IPadContentView contentView) {

	}

	@Override
	public IPadViewControllerV2 getViewController() {
		return null;
	}

	@Override
	public Node getRootNode() {
		return new Label("Test");
	}

	@Override
	public void enableDragAndDropDesignMode(boolean enable) {

	}
	
	@Override
	public void showBusyView(boolean enable) {
		
	}

}
