package de.tobias.playpad.action.feedback;

import de.tobias.playpad.pad.Pad;

/**
 * Eine Action implementiert dieses Interface, falls die Feedbackfarbe automatisch an die Farbe der Kachel angepasst werden soll.
 * 
 * @author tobias
 * @since 5.0.0
 *
 */
public interface ColorAdjustable {

	/**
	 * Ist dieses Feature ative.
	 * 
	 * @return <code>true</code> Active
	 */
	public boolean isAutoFeedbackColors();

	/**
	 * Kachel, die mit dieser Action verkünpft ist.
	 * 
	 * @return Pad
	 */
	public Pad getPad();

}
