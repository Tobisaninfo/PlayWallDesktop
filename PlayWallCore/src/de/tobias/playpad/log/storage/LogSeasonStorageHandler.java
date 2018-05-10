package de.tobias.playpad.log.storage;

import de.tobias.playpad.log.LogItem;
import de.tobias.playpad.log.LogSeason;
import de.tobias.playpad.log.PlayOutItem;

import java.util.List;

public interface LogSeasonStorageHandler {
	void addLogSeason(LogSeason season);

	void addLogItem(LogItem item);

	void addPlayOutItem(PlayOutItem item);

	LogSeason getLogSeason(int id);

	List<LogSeason> getAllLogSeasonsLazy();

	/**
	 * Close the storage handler.
	 *
	 * @throws RuntimeException fail to close handler (e.g. sql error)
	 */
	void close() throws RuntimeException;
}
