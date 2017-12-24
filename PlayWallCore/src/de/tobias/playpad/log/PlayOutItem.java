package de.tobias.playpad.log;

import java.util.UUID;

public class PlayOutItem {

	private final UUID pathUuid;
	private final long time;

	public PlayOutItem(UUID pathUuid, long time) {
		this.pathUuid = pathUuid;
		this.time = time;
	}

	public UUID getPathUuid() {
		return pathUuid;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "PlayOutItem{" +
				"pathUuid=" + pathUuid +
				", time=" + time +
				'}';
	}
}
