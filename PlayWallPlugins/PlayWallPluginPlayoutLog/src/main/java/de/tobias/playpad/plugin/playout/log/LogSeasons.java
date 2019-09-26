package de.tobias.playpad.plugin.playout.log;

import de.tobias.playpad.plugin.playout.log.storage.LogSeasonStorageHandler;

import java.util.ArrayList;
import java.util.List;

public class LogSeasons {

	private static LogSeason currentLogSession;
	private static LogSeasonStorageHandler storageHandler;

	private static List<LogSessionListener> listeners = new ArrayList<>();

	private LogSeasons() {

	}

	public static LogSeason getCurrentSession() {
		return currentLogSession;
	}

	public static LogSeason createLogSeason(String name, int columns, int rows) {
		currentLogSession = new LogSeason(name, columns, rows);

		// Save
		LogSeasonStorageHandler storageHandler = LogSeasons.getStorageHandler();
		if (storageHandler != null) {
			storageHandler.addLogSeason(currentLogSession);
		}

		listeners.forEach(l -> l.playoutLogStarted(currentLogSession));

		return currentLogSession;
	}

	public static LogSeasonStorageHandler getStorageHandler() {
		return storageHandler;
	}

	public static void setStorageHandler(LogSeasonStorageHandler storageHandler) {
		LogSeasons.storageHandler = storageHandler;
	}

	public static void stop() {
		listeners.forEach(l -> l.playoutLogStopped(currentLogSession));
		currentLogSession = null;
	}

	public static List<LogSeason> getAllLogSeasonsLazy() {
		return getStorageHandler().getAllLogSeasonsLazy();
	}

	public static void deleteSession(int id) {
		getStorageHandler().deleteSession(id);
	}

	public static LogSeason getLogSeason(int id) {
		return getStorageHandler().getLogSeason(id);
	}

	/*
	Listener
	 */

	public static void addListener(LogSessionListener logSessionListener) {
		listeners.add(logSessionListener);
	}

	public static void removeListener(LogSessionListener logSessionListener) {
		listeners.remove(logSessionListener);
	}
}
