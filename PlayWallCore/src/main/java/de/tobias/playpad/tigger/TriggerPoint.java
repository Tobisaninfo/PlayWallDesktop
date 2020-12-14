package de.tobias.playpad.tigger;

public enum TriggerPoint {

	START(true),
	STOP(false),
	EOF(true),
	EOF_STATE(false);

	/**
	 * Defines if a trigger can be run after, before a certain event.
	 */
	private final boolean timeAppendable;

	TriggerPoint(boolean timeAppendable) {
		this.timeAppendable = timeAppendable;
	}

	public boolean isTimeAppendable() {
		return timeAppendable;
	}
}
