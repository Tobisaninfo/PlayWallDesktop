package de.tobias.playpad.plugin.playout.log;

import de.tobias.playpad.plugin.playout.log.storage.LogSeasonStorageHandler;

import java.util.List;

public class LogSeasons {

	private static LogSeason INSTANCE;
	private static LogSeasonStorageHandler storageHandler;

	public static LogSeason getInstance() {
		return INSTANCE;
	}

	public static LogSeason createLogSeason(String name, int columns, int rows) {
		INSTANCE = new LogSeason(name, columns, rows);

		// Save
		LogSeasonStorageHandler storageHandler = LogSeasons.getStorageHandler();
		if (storageHandler != null) {
			storageHandler.addLogSeason(INSTANCE);
		}

		return INSTANCE;
	}

	public static LogSeasonStorageHandler getStorageHandler() {
		return storageHandler;
	}

	public static void setStorageHandler(LogSeasonStorageHandler storageHandler) {
		LogSeasons.storageHandler = storageHandler;
	}

	public static void stop() {
		INSTANCE = null;
	}

	public static List<LogSeason> getAllLogSeasonsLazy() {
		return getStorageHandler().getAllLogSeasonsLazy();
	}

	public static void deleteSession(int id) {
		getStorageHandler().deleteSession(id);
	};

	public static LogSeason getLogSeason(int id) {
		return getStorageHandler().getLogSeason(id);
	}
}
