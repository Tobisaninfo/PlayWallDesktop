package de.tobias.playpad.server.sync.conflict;

/**
 * Created by tobias on 30.03.17.
 */
public class Version {
	private final long timestamp;
	private final String sessionName;
	private final boolean local;

	public Version(long timestamp, String sessionName, boolean local) {
		this.timestamp = timestamp;
		this.sessionName = sessionName;
		this.local = local;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getSessionName() {
		return sessionName;
	}

	public boolean isLocal() {
		return local;
	}
}
