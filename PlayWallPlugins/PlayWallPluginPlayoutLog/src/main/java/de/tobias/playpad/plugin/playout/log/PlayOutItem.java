package de.tobias.playpad.plugin.playout.log;

import java.util.UUID;

public class PlayOutItem {

	private final UUID pathUuid;
	private final LogSeason logSeason;
	private final long time;

	public PlayOutItem(UUID pathUuid, LogSeason logSeason, long time) {
		this.pathUuid = pathUuid;
		this.logSeason = logSeason;
		this.time = time;
	}

	public UUID getPathUuid() {
		return pathUuid;
	}

	public LogSeason getLogSeason() {
		return logSeason;
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
