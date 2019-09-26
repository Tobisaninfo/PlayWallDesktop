package de.tobias.playpad.plugin.playout.log;

import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.plugin.playout.log.storage.LogSeasonStorageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogItem {

	private final UUID uuid;
	private final String name;
	private final String color;

	private final int page;
	private final int position;

	private final LogSeason logSeason;

	private final List<PlayOutItem> playOutItems;

	public LogItem(MediaPath mediaPath, LogSeason logSeason) {
		this(
				mediaPath.getId(),
				mediaPath.getPad().getName(),
				mediaPath.getPad().getPadSettings().getBackgroundColor().getColorLow(),
				mediaPath.getPad().getPage().getPosition(),
				mediaPath.getPad().getPosition(),
				logSeason
		);
	}

	public LogItem(UUID uuid, String name, String color, int page, int position, LogSeason logSeason) {
		this(uuid, name, color, page, position, new ArrayList<>(), logSeason);
	}

	public LogItem(UUID uuid, String name, String color, int page, int position, List<PlayOutItem> playOutItems, LogSeason logSeason) {
		this.uuid = uuid;
		this.name = name;
		this.color = color;
		this.page = page;
		this.position = position;
		this.playOutItems = playOutItems;
		this.logSeason = logSeason;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public int getPage() {
		return page;
	}

	public int getPosition() {
		return position;
	}

	public LogSeason getLogSeason() {
		return logSeason;
	}

	public void addPlayOutItem(PlayOutItem item) {
		// Save
		LogSeasonStorageHandler storageHandler = LogSeasons.getStorageHandler();
		if (storageHandler != null) {
			storageHandler.addPlayOutItem(item);
		}
		getPlayOutItems().add(item);
	}

	public List<PlayOutItem> getPlayOutItems() {
		return playOutItems;
	}
}
