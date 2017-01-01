package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadContentListener implements ChangeListener<PadContent> {

	private Pad pad;
	private IPadViewController controller;

	public PadContentListener(IPadViewController controller) {
		this.controller = controller;
	}

	public void setPad(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadContent> observable, PadContent oldValue, PadContent newValue) {
		// wenn Content change, update preview & buttons
		controller.getView().setContentView(pad);
		controller.getView().addDefaultElements(pad);

		controller.updateButtonDisable();
		controller.updateTimeLabel();

		// Remove old listener
		if (oldValue != null && oldValue instanceof Durationable) {
			Durationable oldDurationable = (Durationable) oldValue;
			oldDurationable.durationProperty().removeListener(controller.getPadDurationListener());
			oldDurationable.positionProperty().removeListener(controller.getPadPositionListener());
		}

		// set new content listener / bindings
		if (newValue instanceof Durationable) {
			controller.getView().setPlaybarVisible(true);

			Durationable durationable = (Durationable) newValue;
			durationable.durationProperty().addListener(controller.getPadDurationListener());
			durationable.positionProperty().addListener(controller.getPadPositionListener());

			// Init Duration
			controller.getPadDurationListener().changed(null, null, durationable.getDuration());
		} else {
			controller.getView().setPlaybarVisible(false);
		}
	}
}
