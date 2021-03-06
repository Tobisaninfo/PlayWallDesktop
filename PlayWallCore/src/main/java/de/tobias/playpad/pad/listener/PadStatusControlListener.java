package de.tobias.playpad.pad.listener;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.play.Pauseable;
import de.tobias.playpad.pad.content.play.Seekable;
import de.tobias.playpad.pad.fade.Fadeable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class PadStatusControlListener implements ChangeListener<PadStatus> {

	private Pad pad;

	// Utils für Single Pad Playing
	private static Pad currentPlayingPad; // Nur wenn ProfileSettings.isMultiplePlayer == false

	public PadStatusControlListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		PadSettings padSettings = pad.getPadSettings();
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		try {
			PlayPadPlugin.getInstance().getPadListener().forEach(listener -> listener.onStatusChange(pad, newValue));
		} catch (Exception e) {
			Logger.error(e);
		}

		if (newValue == PadStatus.PLAY) {
			if (pad.getContent() != null) {

				pad.getProject().updateActivePlayerProperty();

				// bei Single Pad Playing wird das alte Pad beendet.
				if (!profileSettings.isMultiplePlayer()) {
					if (currentPlayingPad != null && currentPlayingPad != pad) {
						if (currentPlayingPad.isPlay() || currentPlayingPad.isPaused()) {
							currentPlayingPad.stop();
						}
					}
					currentPlayingPad = pad;
				}

				if (pad.getContent() instanceof Fadeable) {
					if (padSettings.getFade().isFadeInStart() || (oldValue == PadStatus.PAUSE && padSettings.getFade().isFadeInPause())) {
						((Fadeable) pad.getContent()).fadeIn();
					}
				}
				pad.getContent().play();
			}
		} else if (newValue == PadStatus.PAUSE) {
			if (pad.getContent() instanceof Pauseable) {
				if (pad.getContent() instanceof Fadeable && padSettings.getFade().isFadeOutPause()) {
					((Fadeable) pad.getContent()).fadeOut(() -> ((Pauseable) pad.getContent()).pause());
				} else {
					((Pauseable) pad.getContent()).pause();
				}
			}
		} else if (newValue == PadStatus.STOP) {
			if (pad.getContent() != null) {
				pad.getProject().updateActivePlayerProperty();

				if (pad.getContent() instanceof Fadeable && !pad.isEof() && padSettings.getFade().isFadeOutStop()) { // Fade nur wenn Pad
					// nicht am ende ist
					((Fadeable) pad.getContent()).fadeOut(() ->
					{
						pad.getContent().stop();
						pad.setStatus(PadStatus.READY);
						pad.getProject().updateActivePlayerProperty();
					});
				} else {
					boolean shouldBeReady = pad.getContent().stop();
					if (shouldBeReady) {
						pad.setStatus(PadStatus.READY);
						pad.getProject().updateActivePlayerProperty();
					}
				}
			}
		} else if (newValue == PadStatus.RESTART) {
			if (pad.getContent() instanceof Seekable) {
				((Seekable) pad.getContent()).seekToStart();
				pad.setStatus(PadStatus.PLAY);
			}
		}
	}
}
