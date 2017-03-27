package de.tobias.playpad.pad.listener;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusListener implements ChangeListener<PadStatus> {

	private IPadViewController controller;

	public PadStatusListener(IPadViewController controller) {
		this.controller = controller;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		controller.updateButtonDisable();
		controller.updateTimeLabel();

		// Reset
		controller.getView().setErrorLabelActive(false);
		controller.getView().showNotFoundIcon(controller.getPad(), false);

		switch (newValue) {
			case PLAY:
				// Reset Warning Feedback for UI
				controller.getPadPositionListener().setSend(false);

				// UI Styling
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, true);
				break;

			case PAUSE:
				controller.getPadPositionListener().stopWaning();
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
				controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
				break;

			case STOP:
				controller.getPadPositionListener().stopWaning();
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
				controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
				controller.getView().setStyle("");
				break;

			case READY:
				controller.getPadPositionListener().stopWaning();
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
				controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
				controller.getView().setStyle(""); // Cleanup from warning UI
				break;

			case ERROR:
				controller.getView().setErrorLabelActive(true);

				controller.getPadPositionListener().stopWaning();
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
				controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
				controller.getView().setStyle(""); // Cleanup from warning UI
				break;

			case NOT_FOUND:
				controller.getView().showNotFoundIcon(controller.getPad(), true);

				controller.getPadPositionListener().stopWaning();
				controller.getView().pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				controller.getView().pseudoClassState(PseudoClasses.FADE_CLASS, false);
				controller.getView().pseudoClassState(PseudoClasses.WARN_CLASS, false);
				controller.getView().setStyle(""); // Cleanup from warning UI
				break;

			case EMPTY:
				controller.getPadPositionListener().stopWaning();
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
