package de.tobias.playpad.pad.listener;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.design.ModernDesignProvider;
import de.tobias.playpad.design.modern.ModernWarningDesignHandler;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.fade.Fadeable;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import de.tobias.playpad.profile.Profile;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadPositionListener implements Runnable, IPadPositionListener {

	private Pad pad;
	private final AbstractPadViewController controller;

	public PadPositionListener(AbstractPadViewController controller) {
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
			boolean isFading = content instanceof Fadeable && ((Fadeable) content).isFadeActive();
			boolean isPlaying = pad.getStatus() == PadStatus.PLAY;

			if (content instanceof Durationable && (isPlaying || isFading)) {

				Durationable durationable = (Durationable) content;
				Duration totalDuration = durationable.getDuration();

				if (totalDuration != null) {
					updateDuration(newValue, durationable, totalDuration);
					CartAction.refreshFeedback(pad);
				}
			}
		}
	}

	private void updateDuration(Duration newValue, Durationable durationable, Duration totalDuration) {
		double value = newValue.toMillis() / totalDuration.toMillis();
		controller.getView().setPlayBarProgress(value);

		Duration cueInDuration = pad.getPadSettings().getCueIn();
		if (cueInDuration != null) {
			if (cueInDuration.greaterThan(newValue)) {
				double cueInProgress = newValue.toMillis() / cueInDuration.toMillis();
				controller.getView().setCueInProgress(cueInProgress);
			} else {
				controller.getView().setCueInProgress(0);
			}
		}

		// Label (Restlaufzeit)
		controller.updateTimeLabel();

		// Warning nur, wenn kein Loop und nur wenn Play, da sonst schon anderer Zustand und Warning nicht mehr richtig
		// zurückgesetzt wird
		if (!pad.getPadSettings().isLoop() && pad.getStatus() == PadStatus.PLAY) {
			// Warning
			Duration warning = pad.getPadSettings().getWarning();
			Duration rest = durationable.getDuration().subtract(newValue);
			double seconds = rest.toSeconds();

			if (warning.toSeconds() > seconds && !send) {
				startWarningThread();
				send = true;
			}

			if (warning.toSeconds() < seconds && send) {
				stopWaning();
				send = false;
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
		final PadSettings padSettings = pad.getPadSettings();
		final Duration warningDuration = padSettings.getWarning();

		final ModernGlobalDesign globalDesign = Profile.currentProfile().getProfileSettings().getDesign();
		final ModernCartDesign cartDesign = padSettings.getDesign();

		final ModernDesignProvider modernDesign = PlayPadMain.getProgramInstance().getModernDesign();

		final ModernWarningDesignHandler handler = modernDesign.warning();
		handler.handleWarning(globalDesign, cartDesign, controller, warningDuration);
	}

	private void startWarningThread() {
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

		final ModernDesignProvider modernDesign = PlayPadMain.getProgramInstance().getModernDesign();

		final ModernWarningDesignHandler handler = modernDesign.warning();
		handler.stopWarning(controller.getPad());
		controller.getView().setStyle("");
	}
}
