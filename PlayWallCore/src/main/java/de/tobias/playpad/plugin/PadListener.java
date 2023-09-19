package de.tobias.playpad.plugin;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.collections.ListChangeListener;

/**
 * Listener für ein Pad.
 *
 * @author tobias
 * @see Pad
 */
public interface PadListener {

	void onNameChanged(Pad pad, String oldValue, String newValue);

	/**
	 * Call then ever the status of a pad will be changed
	 *
	 * @param pad      corresponding pad
	 * @param newValue new status value
	 */
	void onStatusChange(Pad pad, PadStatus newValue);

	default void onMediaPathChanged(Pad pad, ListChangeListener.Change<? extends MediaPath> value) {
	}

}