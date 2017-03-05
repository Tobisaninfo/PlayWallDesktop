package de.tobias.playpad.server.sync.conflict;

/**
 * Created by tobias on 01.03.17.
 */
public enum ConflictStrategyType {
	/**
	 * Send client changes to server without asking user. (No conflicts).
	 */
	UPDATE,
	/**
	 * Push client changes to server with possible overwritten server data.
	 */
	UPGRADE,
	/**
	 * Discard client changes and reload the project from server.
	 */
	ROLLBACK
}
