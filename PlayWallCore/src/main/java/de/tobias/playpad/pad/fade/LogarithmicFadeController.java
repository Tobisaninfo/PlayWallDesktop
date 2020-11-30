package de.tobias.playpad.pad.fade;

import javafx.animation.Transition;

/**
 * A fade controller implementation, that handles fade scala logarithmic for dB.
 *
 * @author tobias
 * @since 6.0.0
 */
public class LogarithmicFadeController extends AbstractFadeController {

	private static final double VELOCITY = 1;

	public LogarithmicFadeController(FadeControllerDelegate fadeDelegate) {
		super(fadeDelegate);
	}

	@Override
	protected void interpolate(Transition transition, double frac, double from, double to) {
		double diff = Math.abs(to - from);
		if (from < to) { // Fade In
			double fade = computeFadeInMultiplier(frac);
			fadeDelegate.onFadeLevelChange(from + fade * diff);
		} else { // Fade Out
			double fade = computeFadeOutMultiplier(frac);
			double newValue = to + fade * diff;
			fadeDelegate.onFadeLevelChange(newValue);
		}
	}

	protected double computeFadeInMultiplier(double frac) {
		return Math.pow(Math.E, VELOCITY * (frac - 1)) * frac;
	}

	protected double computeFadeOutMultiplier(double frac) {
		return Math.pow(Math.E, -VELOCITY * frac) * (1 - frac);
	}

}
