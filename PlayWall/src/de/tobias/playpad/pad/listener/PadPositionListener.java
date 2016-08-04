package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Durationable;
import de.tobias.playpad.pad.conntent.play.Fadeable;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.Warning;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadPositionListener implements Runnable, IPadPositionListener {

	private Pad pad;
	private IPadViewControllerV2 controller;

	public PadPositionListener(IPadViewControllerV2 controller) {
		this.controller = controller;
	}

	@Override
	public void setPad(Pad pad) {
		this.pad = pad;
	}

	boolean send = false;
	private Thread warningThread;

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		if (oldValue == Duration.ZERO) {
			setSend(false);
		}

		// Progressbar (Prozente)
		if (pad != null) {
			PadContent content = pad.getContent();

			// Zeit aktualiesieren bei Play und wenn Fade Out ist
			boolean isFading = content instanceof Fadeable && ((Fadeable) content).isFading();
			boolean isPlaying = pad.getStatus() == PadStatus.PLAY;

			if (content instanceof Durationable && (isPlaying || isFading)) {

				Durationable durationable = (Durationable) content;
				Duration totalDuration = durationable.getDuration();

				if (totalDuration != null) {
					updateDuration(newValue, durationable, totalDuration);
				}
			}
		}
	}

	private void updateDuration(Duration newValue, Durationable durationable, Duration totalDuration) {
		double value = newValue.toMillis() / totalDuration.toMillis();
		controller.getView().setPlayBarProgress(value);

		// Label (Restlaufzeit)
		controller.updateTimeLabel();

		// Warning nur wenn kein Loop und nur wenn Play, da sonst schon anderer Zustand und Warning nicht mehr richtig Reseted
		// wird
		if (!pad.isLoop() && pad.getStatus() == PadStatus.PLAY) {
			// Warning
			Warning warning = pad.getWarning();
			Duration rest = durationable.getDuration().subtract(newValue);
			double seconds = rest.toSeconds();

			if (warning.getTime().toSeconds() > seconds && !send) {
				startWarningThread();
				send = true;
			}
		}
	}

	@Override
	public void setSend(boolean send) {
		this.send = send;
	}

	/*
	 * EoF GUI Flash
	 */
	@Override
	public void run() {
		Warning warning = pad.getWarning();

		if (pad.isCustomLayout()) {
			pad.getLayout().handleWarning(controller, warning, Profile.currentProfile().currentLayout());
		} else {
			Profile.currentProfile().currentLayout().handleWarning(controller, warning);
		}
	}

	protected void startWarningThread() {
		if (warningThread != null) {
			warningThread.interrupt();
		}
		warningThread = new Thread(this);
		warningThread.start();
	}

	@Override
	public void stopWaning() {
		if (warningThread != null) {
			warningThread.interrupt();
			warningThread = null;
		}

		if (pad.isCustomLayout()) {
			pad.getLayout().stopWarning(controller);
		} else {
			Profile.currentProfile().currentLayout().stopWarning(controller);
		}
		controller.getView().setStyle("");
	}
}
