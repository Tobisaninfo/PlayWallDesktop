package de.tobias.playpad.log;

import de.tobias.playpad.log.storage.LogSeasonStorageHandler;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.project.Project;

import java.util.ArrayList;
import java.util.List;

public class LogSeason {

	private int id;
	private String name;

	private List<LogItem> logItems;

	public LogSeason(String name) {
		this(-1, name);
	}

	public LogSeason(int id, String name) {
		this(id, name, new ArrayList<>());
	}

	public LogSeason(int id, String name, List<LogItem> logItems) {
		this.id = id;
		this.name = name;
		this.logItems = logItems;
	}

	public void createProjectSnapshot(Project project) {
		project.getPads().forEach(this::addLogItem);
	}

	public void addLogItem(Pad pad) {
		for (MediaPath mediaPath : pad.getPaths()) {
			addLogItem(mediaPath);
		}
	}

	public void addLogItem(MediaPath mediaPath) {
		LogItem logItem = new LogItem(mediaPath, this);
		logItems.add(logItem);

		// Save
		LogSeasonStorageHandler storageHandler = LogSeasons.getStorageHandler();
		if (storageHandler != null) {
			storageHandler.addLogItem(logItem);
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<LogItem> getLogItems() {
		return logItems;
	}

	public void setId(int id) {
		this.id = id;
	}
}
