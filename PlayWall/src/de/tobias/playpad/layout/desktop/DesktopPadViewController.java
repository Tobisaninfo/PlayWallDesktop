package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.listener.PadContentListener;
import de.tobias.playpad.pad.listener.PadDurationListener;
import de.tobias.playpad.pad.listener.PadLockedListener;
import de.tobias.playpad.pad.listener.PadPositionListener;
import de.tobias.playpad.pad.listener.PadStatusListener;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.option.pad.PadSettingsViewController;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class DesktopPadViewController implements IPadViewControllerV2, EventHandler<ActionEvent> {

	private DesktopPadView padView;
	private Pad pad;

	private PadLockedListener padLockedListener;
	private PadStatusListener padStatusListener;
	private PadContentListener padContentListener;
	private PadDurationListener padDurationListener;
	private PadPositionListener padPositionListener;

	private PadDragListener padDragListener;
	private transient PadSettingsViewController padSettingsViewController;

	public DesktopPadViewController(DesktopPadView padView) {
		this.padView = padView;

		padLockedListener = new PadLockedListener(this);
		padStatusListener = new PadStatusListener(this);
		padContentListener = new PadContentListener(this);
		padDurationListener = new PadDurationListener(this);
		padPositionListener = new PadPositionListener(this);

		// Listener muss nur einmal hier hinzugefügt werden, weil bei einem neuen Profile, werden neue PadViewController erzeugt
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		profileSettings.lockedProperty().addListener(padLockedListener);
	}

	@Override
	public Pad getPad() {
		return pad;
	}

	@Override
	public IPadViewV2 getView() {
		return padView;
	}

	@Override
	public void setupPad(Pad pad) {
		pad.setController(this);

		this.pad = pad;

		try {
			// Settings
			padView.setIndex(pad.getIndexReadable());
			padView.loopLabelVisibleProperty().bind(pad.loopProperty());

			padView.setTriggerLabelActive(pad.hasTriggerItems());

			// Update Listener
			padContentListener.setPad(pad);
			padPositionListener.setPad(pad);

			// Pad Content Chnage
			pad.contentProperty().addListener(padContentListener);
			// Pad Status Change
			pad.statusProperty().addListener(padStatusListener);

			// First Listener call with new data
			padContentListener.changed(null, null, pad.getContent()); // Add Duration listener
			padStatusListener.changed(null, null, pad.getStatus());

			padDragListener = new PadDragListener(pad, padView);
		} catch (Exception e) {
			e.printStackTrace();
		}
		padView.applyStyleClasses();
		padView.setContentView(pad);
	}

	@Override
	public void removePad() {
		if (padView != null && pad != null)
			padView.removeStyleClasses();

		pad = null;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == padView.getPlayButton()) {
			onPlay();
		} else if (event.getSource() == padView.getPauseButton()) {
			onPause();
		} else if (event.getSource() == padView.getStopButton()) {
			onStop();
		} else if (event.getSource() == padView.getNewButton()) {
			onNew(event);
		} else if (event.getSource() == padView.getSettingsButton()) {
			onSettings();
		}
	}

	private void onPlay() {
		if (pad.getContent() != null) {
			pad.setStatus(PadStatus.PLAY);
		}
	}

	private void onPause() {
		if (pad.getContent() != null) {
			pad.setStatus(PadStatus.PAUSE);
		}
	}

	private void onStop() {
		if (pad.getContent() != null) {
			pad.setStatus(PadStatus.STOP);
		}
	}

	private void onNew(ActionEvent event) {

	}

	private void onSettings() {

	}

	@Override
	public void updateTimeLabel() {

	}

	@Override
	public void updateButtonDisable() {
		if (pad == null) {
			return;
		}
		if (pad.getContent() != null) {
			if (pad.getStatus() == PadStatus.PLAY) {
				padView.getPlayButton().setDisable(true);
				padView.getPauseButton().setDisable(false);
				padView.getStopButton().setDisable(false);
				padView.getNewButton().setDisable(true);
				padView.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.PAUSE) {
				padView.getPlayButton().setDisable(false);
				padView.getPauseButton().setDisable(true);
				padView.getStopButton().setDisable(false);
				padView.getNewButton().setDisable(true);
				padView.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.STOP) {
				padView.getPlayButton().setDisable(false);
				padView.getPauseButton().setDisable(true);
				padView.getStopButton().setDisable(true);
				padView.getNewButton().setDisable(true);
				padView.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.READY) {
				padView.getPlayButton().setDisable(false);
				padView.getPauseButton().setDisable(true);
				padView.getStopButton().setDisable(true);
				padView.getNewButton().setDisable(false);
				padView.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.ERROR) {
				padView.getPlayButton().setDisable(true);
				padView.getPauseButton().setDisable(true);
				padView.getStopButton().setDisable(true);
				padView.getNewButton().setDisable(false);
				padView.getSettingsButton().setDisable(false);
			}
		} else if (pad.getStatus() == PadStatus.EMPTY || pad.getStatus() == PadStatus.ERROR || pad.getContent() == null
				|| !pad.getContent().isPadLoaded()) {
			padView.getPlayButton().setDisable(true);
			padView.getPauseButton().setDisable(true);
			padView.getStopButton().setDisable(true);
			padView.getNewButton().setDisable(false);
			padView.getSettingsButton().setDisable(false);
		}

		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			padView.getNewButton().setDisable(true);
			padView.getSettingsButton().setDisable(true);
		}
	}

}
