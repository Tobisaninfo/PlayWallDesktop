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
import de.tobias.playpad.settings.FadeSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadStatusControlListener implements ChangeListener<PadStatus> {

	private final Pad pad;

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
			PlayPadPlugin.getInstance().getPadListener().forEach(listener -> listener.onStatusChange(pad, oldValue, newValue));
		} catch (Exception e) {
			Logger.error(e);
		}

		if (newValue == PadStatus.PLAY) {
			if (pad.getContent() != null) {

				pad.getProject().updateActivePlayerProperty();

				// bei Single Pad Playing wird das alte Pad beendet.
				// Und wenn das neu abzuspielende Pad nicht im Modus "PlayOverlay" ist.
				if (!profileSettings.isMultiplePlayer() && !pad.getPadSettings().isPlayOverlay()) {
					if (currentPlayingPad != null && currentPlayingPad != pad) {
						if (currentPlayingPad.isPlay() || currentPlayingPad.isPaused()) {
							currentPlayingPad.stop();
						}
					}
					currentPlayingPad = pad;
				}

				boolean withFadeIn = false;
				if (pad.getContent() instanceof Fadeable) {
					final FadeSettings fadeSettings = padSettings.getFade();
					if ((oldValue != PadStatus.PAUSE && fadeSettings.isFadeInStart()) || (oldValue == PadStatus.PAUSE && fadeSettings.isFadeInPause())) {
						if (fadeSettings.getFadeIn().greaterThanOrEqualTo(Duration.seconds(0.1))) { // A fade in less than 0.1s is not recognizable
							final Fadeable fadeable = (Fadeable) pad.getContent();
							fadeable.fadeIn();
							withFadeIn = true;
						}
					}
				}
				pad.getContent().play(withFadeIn);
			}
		} else if (newValue == PadStatus.PAUSE) {
			if (pad.getContent() instanceof Pauseable) {
				final FadeSettings fadeSettings = padSettings.getFade();
				if (pad.getContent() instanceof Fadeable && fadeSettings.isFadeOutPause()) {
					if (fadeSettings.getFadeOut().greaterThanOrEqualTo(Duration.seconds(0.1))) { // A fade in less than 0.1s is not recognizable
						((Fadeable) pad.getContent()).fadeOut(() -> ((Pauseable) pad.getContent()).pause());
						return;
					}
				}
				((Pauseable) pad.getContent()).pause();
			}
		} else if (newValue == PadStatus.STOP) {
			if (pad.getContent() != null) {
				pad.getProject().updateActivePlayerProperty();

				// Fade nur wenn Pad nicht am Ende ist
				if (pad.getContent() instanceof Fadeable && !pad.isEof() && padSettings.getFade().isFadeOutStop()) {
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
