package de.tobias.playpad.log;

import de.tobias.playpad.pad.mediapath.MediaPath;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LogItem {

	private final UUID uuid;
	private final String name;
	private final String color;

	private final int page;
	private final int position;

	private final List<PlayOutItem> playoutItems;

	public LogItem(MediaPath mediaPath) {
		this(
				mediaPath.getId(),
				mediaPath.getPad().getName(),
				mediaPath.getPad().getPadSettings().getDesign().getBackgroundColor().getColorHi(),
				mediaPath.getPad().getPage().getPosition(),
				mediaPath.getPad().getPosition()
		);
	}

	public LogItem(UUID uuid, String name, String color, int page, int position) {
		this(uuid, name, color, page, position, new ArrayList<>());
	}

	public LogItem(UUID uuid, String name, String color, int page, int position, List<PlayOutItem> playoutItems) {
		this.uuid = uuid;
		this.name = name;
		this.color = color;
		this.page = page;
		this.position = position;
		this.playoutItems = playoutItems;
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

	public List<PlayOutItem> getPlayoutItems() {
		return playoutItems;
	}
}
