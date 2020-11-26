package de.tobias.playpad.pad.fade.listener;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.fade.Fadeable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadFadeDurationListener implements ChangeListener<Duration> {

	private final Pad pad;

	public PadFadeDurationListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (pad.getPadSettings().getFade().isFadeOutEof()) {
			final Duration fadeDuration = pad.getPadSettings().getFade().getFadeOut();

			if (pad.getContent() instanceof Durationable) {
				final Durationable durationable = (Durationable) pad.getContent();

				final Duration position = durationable.getPosition();
				final Duration duration = durationable.getDuration();

				if (position != null && duration != null && position.add(fadeDuration).greaterThan(duration)) {
					fadeOut();
				}
			}
		}
	}

	private void fadeOut() {
		if (pad.getContent() instanceof Fadeable) {
			final Fadeable fadeable = (Fadeable) pad.getContent();
			if (!fadeable.isFadeActive()) {
				fadeable.fadeOut();
			}
		}
	}
}
