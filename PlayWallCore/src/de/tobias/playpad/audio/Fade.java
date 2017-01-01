package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.play.FadeHandler;
import javafx.animation.Transition;
import javafx.util.Duration;

/**
 * Fade utils.
 *
 * @author tobias
 * @since 6.0.0
 */
public class Fade {

	private FadeHandler fadeHandler;
	private Transition currentFadeTransition;

	private double velocity = 1;

	public Fade(FadeHandler fadeHandler) {
		this.fadeHandler = fadeHandler;
	}

	public void fadeIn(Duration duration) {
		fade(0, 1, duration, null);
	}

	public void fadeIn(Duration duration, Runnable onFinsih) {
		fade(0, 1, duration, onFinsih);
	}

	public void fadeOut(Duration duration) {
		fade(1, 0, duration, null);
	}

	public void fadeOut(Duration duration, Runnable onFinish) {
		fade(1, 0, duration, onFinish);
	}

	public boolean isFading() {
		return currentFadeTransition != null;
	}

	public void stop() {
		if (currentFadeTransition != null) {
			currentFadeTransition.stop();
		}
	}

	private void fade(double from, double to, Duration duration, Runnable onFinish) {
		if (currentFadeTransition != null) {
			currentFadeTransition.stop();
		}

		currentFadeTransition = new Transition() {

			{
				setCycleDuration(duration);
			}

			@Override
			protected void interpolate(double frac) {
				double diff = Math.abs(to - from);
				if (from < to) { // Fade In
					double fade = fadeInVolumeMultiplier(frac, velocity);
					fadeHandler.setFadeLevel(from + fade * diff);
				} else { // Fade Out
					double fade = fadeOutVolumeMultiplier(frac, velocity);
					double newValue = to + fade * diff;
					fadeHandler.setFadeLevel(newValue);
				}
			}
		};
		currentFadeTransition.setOnFinished(e ->
		{
			currentFadeTransition = null;
			if (onFinish != null) {
				onFinish.run();
			}
		});
		currentFadeTransition.play();
	}

	private double fadeInVolumeMultiplier(double time, double velocity) {
		return Math.pow(Math.E, velocity * (time - 1)) * time;
	}

	private double fadeOutVolumeMultiplier(double time, double velocity) {
		return Math.pow(Math.E, -velocity * time) * (1 - time);
	}

}
