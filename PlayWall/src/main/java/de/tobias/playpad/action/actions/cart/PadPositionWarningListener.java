package de.tobias.playpad.action.actions.cart;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.play.Durationable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadPositionWarningListener implements ChangeListener<Duration> {

	private Pad pad;

	private boolean send = false;

	public PadPositionWarningListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (pad != null && pad.isPadVisible()) {
			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				PadSettings padSettings = pad.getPadSettings();

				if (padSettings.isLoop()) {
					return;
				}

				Duration warning = padSettings.getWarning();
				Duration totalDuration = durationable.getDuration();
				if (totalDuration != null) {
					Duration rest = totalDuration.subtract(newValue);
					double seconds = rest.toSeconds();

					if (warning.toSeconds() > seconds && !send) {
						CartAction.refreshFeedback(pad);
						send = true;
					}
				}
			}
		}
	}

	public void setSend(boolean send) {
		this.send = send;
	}

}
