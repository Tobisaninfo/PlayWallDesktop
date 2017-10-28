package de.tobias.playpad.pad.fade.listener;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.fade.Fadeable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadFadeDurationListener implements ChangeListener<Duration> {

	private Pad pad;

	public PadFadeDurationListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (pad.getPadSettings().getFade().isFadeOutStop()) {
			final Duration fadeDuration = pad.getPadSettings().getFade().getFadeOut();

			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				if (durationable.getPosition() != null && durationable.getDuration() != null) {
					if (durationable.getPosition().add(fadeDuration).greaterThan(durationable.getDuration())) {
						fadeOut();
					}
				}
			}
		}
	}

	private void fadeOut() {
		if (pad.getContent() instanceof Fadeable) {
			Fadeable fadeable = (Fadeable) pad.getContent();
			if (!fadeable.isFadeActive()) {
				fadeable.fadeOut(null);
			}

		}
	}
}
