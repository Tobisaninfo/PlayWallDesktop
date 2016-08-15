package de.tobias.playpad.audio.fade;

import de.tobias.playpad.pad.conntent.play.IVolume;
import javafx.animation.Transition;
import javafx.util.Duration;

/**
 * Fading utils.
 * 
 * @author tobias
 * 
 * @since 6.0.0
 */
public class Fading {

	private IVolume iVolume;
	private Transition currentFadeTransition;

	public Fading(IVolume iVolume) {
		this.iVolume = iVolume;
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
					 double fade = fadeInVolumeMultiplier(frac, 2);
					iVolume.setFadeLevel(from + fade * diff);
				} else { // Fade Out
					double fade = fadeOutVolumeMultiplier(frac, 2);
					double newValue = to + fade * diff;
					iVolume.setFadeLevel(newValue);
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

	protected double fadeInVolumeMultiplier(double time, double velocity) {
		return Math.pow(Math.E, velocity * (time - 1)) * time;
	}

	protected double fadeOutVolumeMultiplier(double time, double velocity) {
		return Math.pow(Math.E, -velocity * time) * (1 - time);
	}

}
