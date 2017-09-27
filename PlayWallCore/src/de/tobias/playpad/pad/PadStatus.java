package de.tobias.playpad.pad;

public enum PadStatus {

	EMPTY,
	ERROR,
	READY,
	// Diese Trigger werden über den Listener PadStatusControlListener realisiert.
	/**
	 * Trigger the player to start playing.
	 */
	PLAY,
	/**
	 * Trigger the player to pause playing.
	 */
	PAUSE,
	/**
	 * Trigger the player to stop playing, not for end of file. To detect end of file, use oldValue = PLAY and newValue = READY.
	 */
	STOP,
	/**
	 * Restart a Pad
	 */
	RESTART,
	/**
	 * File not found
	 */
	NOT_FOUND
}