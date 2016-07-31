package de.tobias.playpad.pad.listener;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusListener implements ChangeListener<PadStatus> {

	private IPadViewControllerV2 controller;

	public PadStatusListener(IPadViewControllerV2 controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		controller.updateButtonDisable();
		controller.updateTimeLabel();
		controller.getView().setErrorLabelActive(false);

		switch (newValue) {
		case PLAY:
			// Reset Warning Feedback for UI
			// controller.getPadPositionListener().setSend(false); TODO Warning

			// UI Styling
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, true);
			break;

		case PAUSE:
			// controller.getPadPositionListener().stopWaning(); TODO Warning
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			break;

		case STOP:
			// controller.getPadPositionListener().stopWaning(); TODO Warning
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getView().setStyle("");
			break;

		case READY:
			// controller.getPadPositionListener().stopWaning(); TODO Warning
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getView().setStyle(""); // Cleanup from warning UI
			break;
		case ERROR:
			controller.getView().setErrorLabelActive(true);

			// controller.getPadPositionListener().stopWaning(); TODO Warning
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getView().setStyle(""); // Cleanup from warning UI
			break;

		case EMPTY:
			// controller.getPadPositionListener().stopWaning(); TODO Warning
			controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
			controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
			controller.getView().setStyle(""); // Cleanup from warning UI
			break;
		default:
			break;
		}
	}
}
