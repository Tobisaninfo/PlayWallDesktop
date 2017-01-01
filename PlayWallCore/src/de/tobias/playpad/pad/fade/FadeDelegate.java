package de.tobias.playpad.pad.fade;

/**
 * Delegates the fade level.
 *
 * @author tobias
 * @since 6.0.0
 */
public interface FadeDelegate {

	/**
	 * If the fade level is changed, the delegate performs this method. The faded object should update.
	 *
	 * @param level New Fadinglevel (0-1)
	 */
	void onFadeLevelChange(double level);
}
