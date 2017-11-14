package de.tobias.playpad.log;

public class LogSeasons {

	private static LogSeason INSTANCE;

	public static LogSeason getInstance() {
		return INSTANCE;
	}

	public static LogSeason createLogSeason(String name) {
		INSTANCE = new LogSeason(name);
		return INSTANCE;
	}
}
