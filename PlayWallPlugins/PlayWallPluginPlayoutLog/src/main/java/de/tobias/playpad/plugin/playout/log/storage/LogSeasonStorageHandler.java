package de.tobias.playpad.plugin.playout.log.storage;

import de.tobias.playpad.plugin.playout.log.LogItem;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.PlayOutItem;

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
	void close();
}
