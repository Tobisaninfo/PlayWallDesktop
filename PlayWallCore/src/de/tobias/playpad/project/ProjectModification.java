package de.tobias.playpad.project;

/**
 * Created by tobias on 01.03.17.
 */
public class ProjectModification {

	private final String session;
	private final long time;

	public ProjectModification(String session, long time) {
		this.session = session;
		this.time = time;
	}

	public String getSession() {
		return session;
	}

	public long getTime() {
		return time;
	}
}
