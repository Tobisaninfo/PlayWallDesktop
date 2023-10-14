package de.tobias.playpad.tigger;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.Playlistable;

import java.util.function.Predicate;

public enum TriggerPoint {

	START(true, pad -> !(pad.getContent() instanceof Playlistable)),
	STOP(false, pad -> !(pad.getContent() instanceof Playlistable)),
	EOF(true,  pad -> !(pad.getContent() instanceof Playlistable)),
	PLAYLIST_START(false,  pad -> pad.getContent() instanceof Playlistable),
	PLAYLIST_ITEM_START(true,  pad -> pad.getContent() instanceof Playlistable),
	PLAYLIST_ITEM_END(false,  pad -> pad.getContent() instanceof Playlistable),
	PLAYLIST_END(false,  pad -> pad.getContent() instanceof Playlistable);

	/**
	 * Defines if a trigger can be run after, before a certain event.
	 */
	private final boolean timeAppendable;
	private final Predicate<Pad> availablePredicate;

	TriggerPoint(boolean timeAppendable, Predicate<Pad> availablePredicate) {
		this.timeAppendable = timeAppendable;
		this.availablePredicate = availablePredicate;
	}

	public boolean isTimeAppendable() {
		return timeAppendable;
	}

	public Predicate<Pad> getAvailablePredicate() {
		return availablePredicate;
	}

	public boolean isAvailable(Pad pad) {
		return availablePredicate.test(pad);
	}
}
