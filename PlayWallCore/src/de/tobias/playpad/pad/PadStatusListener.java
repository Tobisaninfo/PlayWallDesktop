package de.tobias.playpad.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.conntent.Fadeable;
import de.tobias.playpad.pad.conntent.Pauseable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusListener implements ChangeListener<PadStatus> {

	private Pad pad;

	public PadStatusListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		if (newValue == PadStatus.PLAY) {
			if (pad.getContent() != null) {
				PlayPadPlugin.getImplementation().getPadListener().forEach(listener -> listener.onPlay(pad));

				if (pad.getContent() instanceof Fadeable) {
					if (oldValue == PadStatus.PAUSE && pad.getFade().isFadeInPause()) {
						((Fadeable) pad.getContent()).fadeIn();
					} else if (pad.getFade().isFadeInStart()) {
						((Fadeable) pad.getContent()).fadeIn();
					}
				}
				pad.getContent().play();
			}
		} else if (newValue == PadStatus.PAUSE) {
			if (pad.getContent() instanceof Pauseable) {
				if (pad.getContent() instanceof Fadeable && pad.getFade().isFadeOutPause()) {
					((Fadeable) pad.getContent()).fadeOut(() ->
					{
						((Pauseable) pad.getContent()).pause();
					});
				} else {
					((Pauseable) pad.getContent()).pause();
				}
			}
		} else if (newValue == PadStatus.STOP) {
			if (pad.getContent() != null) {
				PlayPadPlugin.getImplementation().getPadListener().forEach(listener -> listener.onStop(pad));

				if (pad.getContent() instanceof Fadeable && !pad.isEof() && pad.getFade().isFadeOutStop()) { // Fade nur wenn pad nicht am
																												// ende ist
					((Fadeable) pad.getContent()).fadeOut(() ->
					{
						pad.getContent().stop();
						pad.setStatus(PadStatus.READY);
					});
				} else {
					boolean shouldBeReady = pad.getContent().stop();
					if (shouldBeReady)
						pad.setStatus(PadStatus.READY);
				}
			}
		}
	}
}
