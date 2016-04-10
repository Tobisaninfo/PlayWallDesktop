package de.tobias.playpad.plugin;

import de.tobias.playpad.pad.Pad;

public interface PadListener {

	public default void onPlay(Pad pad) {}

	public default void onStop(Pad pad) {}

}