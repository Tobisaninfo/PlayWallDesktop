package de.tobias.playpad.pad.fade.listener;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.fade.Fadeable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadFadeContentListener implements ChangeListener<PadContent> {

	private Pad pad;

	public PadFadeContentListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadContent> observable, PadContent oldValue, PadContent newValue) {
		if (oldValue != null) {
			if (oldValue instanceof Durationable && oldValue instanceof Fadeable) {
				((Durationable) oldValue).positionProperty().removeListener(pad.getPadFadeDurationListener());
			}
		}

		if (newValue != null) {
			if (newValue instanceof Durationable && newValue instanceof Fadeable) {
				((Durationable) newValue).positionProperty().addListener(pad.getPadFadeDurationListener());
			}
		}
	}
}
