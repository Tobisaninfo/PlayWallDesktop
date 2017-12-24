package de.tobias.playpad.log;

import de.tobias.playpad.log.storage.LogSeasonStorageHandler;

public class LogSeasons {

	private static LogSeason INSTANCE;
	private static LogSeasonStorageHandler storageHandler;

	public static LogSeason getInstance() {
		return INSTANCE;
	}

	public static LogSeason createLogSeason(String name) {
		INSTANCE = new LogSeason(name);

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
}
