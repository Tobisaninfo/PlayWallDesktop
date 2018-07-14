package de.tobias.playpad.server;

/**
 * Created by tobias on 21.02.17.
 */
public interface SessionDelegate {

	/**
	 * Return a new session from the server and saves it on disk.
	 *
	 * @return session
	 */
	Session getSession();
}
