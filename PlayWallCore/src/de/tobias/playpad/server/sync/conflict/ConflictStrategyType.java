package de.tobias.playpad.server.sync.conflict;

/**
 * Created by tobias on 01.03.17.
 */
public enum ConflictStrategyType {
	/**
	 * Push client changes to server with possible overwritten server data.
	 */
	UPGRADE,
	/**
	 * Discard client changes and reload the project from server.
	 */
	ROLLBACK
}
