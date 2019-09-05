package de.tobias.playpad.plugin;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;

/**
 * Listener für ein Pad.
 *
 * @author tobias
 * @see Pad
 */
public interface PadListener {

	/**
	 * Call then ever the status of a pad will be changed
	 * @param pad corresponding pad
	 * @param newValue new status value
	 */
	void onStatusChange(Pad pad, PadStatus newValue);

}