package de.tobias.playpad.log.listener;

import de.tobias.playpad.log.LogSeason;
import de.tobias.playpad.log.LogSeasons;
import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.collections.ListChangeListener;

public class PadMediaPathLogListener implements ListChangeListener<MediaPath> {
	@Override
	public void onChanged(Change<? extends MediaPath> c) {
		LogSeason instance = LogSeasons.getInstance();
		if (instance != null) {
			while (c.next()) {
				if (c.wasAdded()) {
					for (MediaPath mediaPath : c.getAddedSubList()) {
						instance.addLogItem(mediaPath);
					}
				}
			}
		}
	}
}
