package de.tobias.playpad.action.cartaction;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Durationable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadContentFeedbackListener implements ChangeListener<PadContent> {

	private CartAction action;

	public void setAction(CartAction action) {
		this.action = action;
	}

	@Override
	public void changed(ObservableValue<? extends PadContent> observable, PadContent oldValue, PadContent newValue) {
		if (oldValue != null) {
			if (oldValue instanceof Durationable) {
				Durationable durationable = (Durationable) oldValue;
				durationable.positionProperty().addListener(action.getPadPositionListener());
			}
		}
		
		if (newValue != null) {
			if (newValue instanceof Durationable) {
				Durationable durationable = (Durationable) newValue;
				durationable.positionProperty().addListener(action.getPadPositionListener());
			}
		}
	}
}
