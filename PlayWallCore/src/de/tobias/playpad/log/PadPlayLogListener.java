package de.tobias.playpad.log;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadPlayLogListener implements ChangeListener<PadStatus> {

	private Pad pad;

	public PadPlayLogListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		if (newValue == PadStatus.PLAY) {
			LogSeason instance = LogSeasons.getInstance();
			instance.getLogItems().stream().filter(item -> item.getUuid().equals(pad.getPaths().get(0).getId())).forEach(item -> {
				PlayOutItem playoutItem = new PlayOutItem(item.getUuid(), System.currentTimeMillis());
				item.getPlayoutItems().add(playoutItem);
				System.out.println(playoutItem);
			});
		}
	}
}
