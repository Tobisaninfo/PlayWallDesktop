package de.tobias.playpad.pad.listener;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.viewcontroller.pad.PadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusListener implements ChangeListener<PadStatus> {

	private PadViewController controller;

	public PadStatusListener(PadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		controller.updateButtonDisable();
		controller.updateTimeLabel();
		controller.getParent().setErrorLabelActive(false);

		switch (newValue) {
		case PLAY:
			// Reset Warning Feedback for UI
			controller.getPadPositionListener().setSend(false);

			// UI Styling
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, true);
			break;

		case PAUSE:
			controller.getPadPositionListener().stopWaning();
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getParent().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getParent().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			break;

		case STOP:
			controller.getPadPositionListener().stopWaning();
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getParent().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getParent().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getParent().setStyle("");
			break;

		case READY:
			controller.getPadPositionListener().stopWaning();
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getParent().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getParent().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getParent().setStyle(""); // Cleanup from warning UI
			break;
		case ERROR:
			controller.getParent().setErrorLabelActive(true);

			controller.getPadPositionListener().stopWaning();
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getParent().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getParent().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getParent().setStyle(""); // Cleanup from warning UI
			break;

		case EMPTY:
			controller.getPadPositionListener().stopWaning();
			controller.getParent().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getParent().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getParent().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getParent().setStyle(""); // Cleanup from warning UI
			break;
		default:
			break;
		}
	}
}
