package de.tobias.playpad.action.cartaction;

import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.Durationable;
import de.tobias.playpad.settings.Warning;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadStatusFeedbackListener implements ChangeListener<PadStatus> {

	private CartAction action;

	public void setAction(CartAction action) {
		this.action = action;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		if (action != null) {
			Pad pad = action.getPad();
			if (pad.isPadVisible()) {
				switch (newValue) {
				case EMPTY:
				case ERROR:
					action.handleFeedback(FeedbackMessage.OFF);
					break;
				case PAUSE:
					action.handleFeedback(FeedbackMessage.STANDARD);
					break;
				case PLAY:
					action.handleFeedback(FeedbackMessage.EVENT);

					// Wenn Cart in Warning Zeitbereich und vomn Pause zu Play wechselt
					try {
						if (pad.getContent() instanceof Durationable) {
							Durationable durationable = (Durationable) pad.getContent();
							if (!pad.isLoop()) {
								Warning warning = pad.getWarning();
								Duration rest = durationable.getDuration().subtract(durationable.getPosition());
								double seconds = rest.toSeconds();

								if (warning.getTime().toSeconds() > seconds) {
									action.handleFeedback(FeedbackMessage.WARNING);
								}
							}
						}
					} catch (Exception e) {}
					break;
				case READY:
					action.handleFeedback(FeedbackMessage.STANDARD);
					break;
				case STOP:
					action.handleFeedback(FeedbackMessage.STANDARD);
					break;
				default:
					break;

				}
			}
		}
	}
}
