package de.tobias.playpad.view.touch;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.pad.PadViewController;
import de.tobias.utils.ui.scene.BusyView;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class PadViewTouch extends StackPane implements IPadView {

	private HBox preview;
	private IPadContentView previewContent;

	private ProgressBar playBar;

	private BusyView busyView;

	private transient PadViewController controller; // Reference to its controller

	public PadViewTouch(PadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void setBusy(boolean busy) {

	}

	@Override
	public void pseudoClassState(PseudoClass pseudoClass, boolean active) {

	}

	@Override
	public Node getNewButton() {
		return null;
	}

	@Override
	public Button getPlayButton() {
		return null;
	}

	@Override
	public Button getPauseButton() {
		return null;
	}

	@Override
	public Button getStopButton() {
		return null;
	}

	@Override
	public ProgressBar getPlayBar() {
		return null;
	}

	@Override
	public void showPlaybar(boolean b) {

	}

	@Override
	public void setPreviewContent(Pad pad) {

	}

	@Override
	public void addDefaultButton(Pad pad) {

	}

	@Override
	public void setErrorLabelActive(boolean b) {

	}

	@Override
	public void setTriggerLabelActive(boolean b) {

	}

	@Override
	public IPadContentView getPadContentView() {
		return null;
	}

}
