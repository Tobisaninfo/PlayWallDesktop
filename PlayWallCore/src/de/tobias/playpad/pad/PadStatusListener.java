package de.tobias.playpad.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.conntent.play.Fadeable;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusListener implements ChangeListener<PadStatus> {

	private Pad pad;

	// Utils für Single Pad Playing
	private static Pad currentPlayingPad; // Nur wenn ProfileSettings.isMultiplePlayer == false

	public PadStatusListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		PadSettings padSettings = pad.getPadSettings();
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		if (newValue == PadStatus.PLAY) {
			if (pad.getContent() != null) {
				PlayPadPlugin.getImplementation().getPadListener().forEach(listener -> listener.onPlay(pad));

				// bei Single Pad Playing wird das alte Pad beendet.
				if (!profileSettings.isMultiplePlayer()) {
					if (currentPlayingPad != null && currentPlayingPad != pad) {
						if (currentPlayingPad.getStatus() == PadStatus.PLAY || currentPlayingPad.getStatus() == PadStatus.PAUSE) {
							currentPlayingPad.setStatus(PadStatus.STOP);
						}
					}
					currentPlayingPad = pad;
				}

				if (pad.getContent() instanceof Fadeable) {
					if (oldValue == PadStatus.PAUSE && padSettings.getFade().isFadeInPause()) {
						((Fadeable) pad.getContent()).fadeIn();
					} else if (padSettings.getFade().isFadeInStart()) {
						((Fadeable) pad.getContent()).fadeIn();
					}
				}
				pad.getContent().play();
			}
		} else if (newValue == PadStatus.PAUSE) {
			if (pad.getContent() instanceof Pauseable) {
				if (pad.getContent() instanceof Fadeable && padSettings.getFade().isFadeOutPause()) {
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

				if (pad.getContent() instanceof Fadeable && !pad.isEof() && padSettings.getFade().isFadeOutStop()) { // Fade nur wenn Pad
																														// nicht am ende ist
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
