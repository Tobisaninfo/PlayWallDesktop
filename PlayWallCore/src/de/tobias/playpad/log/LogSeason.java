package de.tobias.playpad.log;

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

	private void addLogItem(Pad pad) {
		for (MediaPath mediaPath : pad.getPaths()) {
			logItems.add(new LogItem(mediaPath));
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
}
