package de.tobias.playpad.pad.listener.trigger;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Durationable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

// Fügt Time Listener hinzu für neuen Content
public class PadTriggerContentListener implements ChangeListener<PadContent> {

	private Pad pad;

	public PadTriggerContentListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadContent> observable, PadContent oldValue, PadContent newValue) {
		if (oldValue != null) {
			if (oldValue instanceof Durationable) {
				((Durationable) oldValue).positionProperty().removeListener(pad.getPadTriggerDurationListener());
			}
		}

		if (newValue != null) {
			if (newValue instanceof Durationable) {
				((Durationable) newValue).positionProperty().addListener(pad.getPadTriggerDurationListener());
			}
		}

	}
}
