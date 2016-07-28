package de.tobias.playpad.plugin;

import de.tobias.playpad.pad.Pad;

/**
 * Listener für ein Pad.
 * 
 * @author tobias
 * 
 * @see Pad
 *
 */
public interface PadListener {

	/**
	 * Wird aufgerufen, sobald ein Pad wiedergegeben wird.
	 * 
	 * @param pad
	 *            Pad
	 */
	public void onPlay(Pad pad);

	/**
	 * Wird aufgerufen, sobald ein Pad gestoppt wird.
	 * 
	 * @param pad
	 *            Pad
	 */
	public void onStop(Pad pad);

}