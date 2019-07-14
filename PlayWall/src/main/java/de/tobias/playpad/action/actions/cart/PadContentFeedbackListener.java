package de.tobias.playpad.action.actions.cart;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadContentFeedbackListener implements ChangeListener<PadContent> {

	private PadPositionWarningListener warningListener;

	public PadContentFeedbackListener(Pad pad) {
		warningListener = new PadPositionWarningListener(pad);
	}

	@Override
	public void changed(ObservableValue<? extends PadContent> observable, PadContent oldValue, PadContent newValue) {
		if (oldValue != null) {
			if (oldValue instanceof Durationable) {
				Durationable durationable = (Durationable) oldValue;
				durationable.positionProperty().addListener(warningListener);
			}
		}

		if (newValue != null) {
			if (newValue instanceof Durationable) {
				Durationable durationable = (Durationable) newValue;
				durationable.positionProperty().addListener(warningListener);
			}
		}
	}
}
