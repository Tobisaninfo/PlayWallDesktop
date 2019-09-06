package de.tobias.playpad.plugin.playout.log.listener;

import de.thecodelabs.logger.LogLevel;
import de.thecodelabs.logger.Logger;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.LogSeasons;
import de.tobias.playpad.plugin.playout.log.PlayOutItem;
import javafx.collections.ListChangeListener;

public class PadPlayLogListener implements PadListener {

	@Override
	public void onStatusChange(Pad pad, PadStatus newValue) {
		if (newValue == PadStatus.PLAY) {
			LogSeason instance = LogSeasons.getInstance();
			if (instance != null) {
				instance.getLogItems().stream().filter(item -> item.getUuid().equals(pad.getPaths().get(0).getId())).forEach(item -> {
					PlayOutItem playoutItem = new PlayOutItem(item.getUuid(), instance, System.currentTimeMillis());
					item.addPlayOutItem(playoutItem);
					Logger.log(LogLevel.DEBUG, "Play Item: " + playoutItem);
				});
			}
		}
	}

	@Override
	public void onMediaPathChanged(Pad pad, ListChangeListener.Change<? extends MediaPath> value) {
		LogSeason instance = LogSeasons.getInstance();
		if (instance != null) {
			while (value.next()) {
				if (value.wasAdded()) {
					for (MediaPath mediaPath : value.getAddedSubList()) {
						instance.addLogItem(mediaPath);
					}
				}
			}
		}
	}
}
