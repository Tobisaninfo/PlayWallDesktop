package de.tobias.playpad.action.actions.cart;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusFeedbackListener implements ChangeListener<PadStatus> {

	private Pad pad;

	public PadStatusFeedbackListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		CartAction.refreshFeedback(pad);
	}
}
