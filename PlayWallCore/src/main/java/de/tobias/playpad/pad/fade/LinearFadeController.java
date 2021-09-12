package de.tobias.playpad.pad.fade;

import javafx.animation.Transition;

/**
 * A fade controller implementation, that handles fade scala linear.
 *
 * @author tobias
 * @since 7.1.0
 */
public class LinearFadeController extends AbstractFadeController {

	public LinearFadeController(FadeControllerDelegate fadeDelegate) {
		super(fadeDelegate);
	}

	@Override
	protected void interpolate(Transition transition, double frac, double from, double to) {
		double diff = Math.abs(to - from);
		if (from < to) { // Fade In
			fadeDelegate.onFadeLevelChange(diff * frac);
		} else { // Fade Out
			fadeDelegate.onFadeLevelChange(from - (diff * frac));
		}
	}
}
