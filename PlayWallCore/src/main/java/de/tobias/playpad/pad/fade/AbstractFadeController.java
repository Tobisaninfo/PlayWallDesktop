package de.tobias.playpad.pad.fade;

import de.thecodelabs.logger.Logger;
import javafx.animation.Transition;
import javafx.util.Duration;

/**
 * @since 7.1.0
 */
public abstract class AbstractFadeController {

	private Transition currentFadeTransition;
	protected final FadeControllerDelegate fadeDelegate;

	public AbstractFadeController(FadeControllerDelegate fadeDelegate) {
		this.fadeDelegate = fadeDelegate;
	}

	public void fadeIn(Duration duration) {
		fade(0, 1, duration, null);
	}

	public void fadeIn(Duration duration, Runnable onFinish) {
		fade(0, 1, duration, onFinish);
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

	public void fade(double from, double to, Duration duration, Runnable onFinish) {
		Logger.debug("Fading from {0} to {1} in {2}", from, to, duration);

		if (currentFadeTransition != null) {
			currentFadeTransition.stop();
		}

		currentFadeTransition = new Transition() {
			{
				setCycleDuration(duration);
			}

			@Override
			protected void interpolate(double frac) {
				AbstractFadeController.this.interpolate(this, frac, from, to);
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

	protected abstract void interpolate(Transition transition, double frac, double from, double to);
}
