package de.tobias.playpad.pad.listener;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadNameChangeListener implements ChangeListener<String> {

	private final Pad pad;

	public PadNameChangeListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		try {
			PlayPadPlugin.getInstance().getPadListener().forEach(listener -> listener.onNameChanged(pad, oldValue, newValue));
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
