package de.tobias.playpad.layout.touch;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.css.PseudoClass;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

public class TouchPadView implements IPadViewV2 {

	@Override
	public IPadContentView getContentView() {
		return null;
	}

	@Override
	public void setContentView(Pad pad) {

	}

	@Override
	public IPadViewControllerV2 getViewController() {
		return null;
	}

	@Override
	public Pane getRootNode() {
		return null;
	}

	@Override
	public void enableDragAndDropDesignMode(boolean enable) {

	}

	@Override
	public void showBusyView(boolean enable) {

	}

	@Override
	public void pseudoClassState(PseudoClass playCalss, boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStyle(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setErrorLabelActive(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public ProgressBar getPlayBar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDefaultButton(Pad pad) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyStyleClasses() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeStyleClasses() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlaybarVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}
}
