package de.tobias.playpad.plugin.playout.log.storage;

import de.tobias.playpad.plugin.playout.log.LogItem;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.PlayOutItem;

import java.util.List;

public interface LogSeasonStorageHandler extends AutoCloseable {

	void addLogSeason(LogSeason season);

	void addLogItem(LogItem item);

	void addPlayOutItem(PlayOutItem item);

	LogSeason getLogSeason(int id);

	List<LogSeason> getAllLogSeasonsLazy();

	void deleteSession(int id);
}
