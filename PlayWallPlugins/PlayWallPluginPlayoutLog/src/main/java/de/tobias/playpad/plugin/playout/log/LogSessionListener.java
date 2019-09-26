package de.tobias.playpad.plugin.playout.log;

public interface LogSessionListener
{
	void playoutLogStarted(LogSeason logSeason);

	void playoutLogStopped(LogSeason logSeason);
}
