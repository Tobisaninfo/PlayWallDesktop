package de.tobias.playpad.viewcontroller.pad;

import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.Warning;
import de.tobias.playpad.settings.Profile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

// TODO Renew class
class PlayDurationEventHandler implements ChangeListener<Duration>, Runnable {

	/**
	 * 
	 */
	private final PadViewController padViewController;

	/**
	 * @param padViewController
	 */
	PlayDurationEventHandler(PadViewController padViewController) {
		this.padViewController = padViewController;
	}

	boolean send = false;

	private Thread warningThread;

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (!send) {
			if (padViewController != null && padViewController.pad != null && !this.padViewController.pad.isLoop()) {
				Duration rest = this.padViewController.pad.getAudioHandler().getDuration(this.padViewController.pad).subtract(newValue);
				double seconds = rest.toSeconds();

				Midi midi = Midi.getInstance();

				if (this.padViewController.midiKeyPressedForPlay != -1) {
					if (this.padViewController.pad.isCustomWarning()) {
						if (this.padViewController.pad.getWarningFeedback().get().getTime().toSeconds() > seconds) {
							startWarningThread();

							if (midi.getMidiDevice().isPresent()) {
								// TODO MIDI Send Feedback
							}
							send = true;
						}
					} else {
						if (Profile.currentProfile().getProfileSettings().getWarningFeedback().getTime().toSeconds() > seconds) {
							startWarningThread();

							if (midi.getMidiDevice().isPresent()) {
								// TODO MIDI Send Feedback
							}
							send = true;
						}
					}
				}
			}
		}
	}

	/*
	 * EoF GUI Flash
	 */
	@Override
	public void run() {
		Pad pad = padViewController.getPad();

		Profile currentProfile = Profile.currentProfile();
		Warning warning = pad.getWarningFeedback().orElseGet(() -> currentProfile.getProfileSettings().getWarningFeedback());
		if (pad.isCustomLayout()) {
			pad.currentLayout().ifPresent(layout -> layout.handleWarning(padViewController, warning));
		} else {
			currentProfile.currentLayout().handleWarning(padViewController, warning);
		}
	}

	protected void startWarningThread() {
		if (warningThread != null) {
			warningThread.interrupt();
		}
		warningThread = new Thread(this);
		warningThread.start();
	}

	protected void stopWaning() {
		if (warningThread != null) {
			warningThread.interrupt();
			padViewController.getView().setStyle("");
			warningThread = null;
		}
	}
}