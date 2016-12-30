package de.tobias.playpad.action.cartaction;

import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.conntent.play.Durationable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadPositionWarningListener implements ChangeListener<Duration> {

	private Pad pad;
	private CartAction action;

	boolean send = false;

	public PadPositionWarningListener(CartAction action) {
		this.action = action;
	}

	public void setPad(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		// Nur wenn Pad Sichtbar ist
		if (pad != null && pad.isPadVisible()) {
			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				PadSettings padSettings = pad.getPadSettings();

				// Warning nur wenn kein Loop
				if (!padSettings.isLoop()) {
					// Warning
					Duration warning = padSettings.getWarning();
					Duration totalDuration = durationable.getDuration();
					if (totalDuration != null) {
						Duration rest = totalDuration.subtract(newValue);
						double seconds = rest.toSeconds();

						if (warning.toSeconds() > seconds && !send) {
							action.handleFeedback(FeedbackMessage.WARNING);
							send = true;
						}
					}
				}
			}
		}
	}

	public void setSend(boolean send) {
		this.send = send;
	}

}
