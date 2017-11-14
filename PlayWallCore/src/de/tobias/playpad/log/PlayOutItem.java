package de.tobias.playpad.log;

import java.util.UUID;

public class PlayOutItem {

	private final UUID pathUUId;
	private final long time;

	public PlayOutItem(UUID pathUUId, long time) {
		this.pathUUId = pathUUId;
		this.time = time;
	}

	public UUID getPathUUId() {
		return pathUUId;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "PlayOutItem{" +
				"pathUUId=" + pathUUId +
				", time=" + time +
				'}';
	}
}
