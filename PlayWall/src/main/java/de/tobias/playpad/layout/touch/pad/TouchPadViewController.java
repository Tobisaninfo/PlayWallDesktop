package de.tobias.playpad.layout.touch.pad;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.listener.*;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.util.Duration;

public class TouchPadViewController extends AbstractPadViewController implements EventHandler<Event> {

	protected static final String CURRENT_PAGE_BUTTON = "current-page-button";
	private static final String DURATION_FORMAT = "%d:%02d";

	private final TouchPadView padView;
	private Pad pad;

	private final PadLockedListener padLockedListener;
	private final PadStatusListener padStatusListener;
	private final PadContentListener padContentListener;
	private final PadDurationListener padDurationListener;
	private final IPadPositionListener padPositionListener;

	TouchPadViewController(TouchPadView padView) {
		this.padView = padView;

		padLockedListener = new PadLockedListener(this);
		padStatusListener = new PadStatusListener(this);
		padContentListener = new PadContentListener(this);
		padDurationListener = new PadDurationListener(this);
		padPositionListener = new PadPositionListener(this);

		// Listener muss nur einmal hier hinzugefügt werden, weil bei einem
		// neuen Profile, werden neue PadViewController
		// erzeugt
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		profileSettings.lockedProperty().addListener(padLockedListener);
	}

	@Override
	public Pad getPad() {
		return pad;
	}

	@Override
	public IPadView getView() {
		return padView;
	}

	@Override
	public void setupPad(Pad pad) {
		pad.setController(this);

		this.pad = pad;

		try {
			// Settings
			padView.setIndex(pad.getPositionReadable());
			padView.loopLabelVisibleProperty().bind(pad.getPadSettings().loopProperty());
			padView.setTriggerLabelActive(pad.getPadSettings().hasTriggerItems());

			updatePlaylistLabelBinding(pad);

			// Update Listener
			padContentListener.setPad(pad);
			padPositionListener.setPad(pad);

			// Pad Content Change
			pad.contentProperty().addListener(padContentListener);
			// Pad Status Change
			pad.statusProperty().addListener(padStatusListener);

			// First Listener call with new data
			padContentListener.changed(null, null, pad.getContent()); // Add Duration listener
			padStatusListener.changed(null, null, pad.getStatus());
		} catch (Exception e) {
			Logger.error(e);
		}

		padView.applyStyleClasses(pad.getPadIndex());
		padView.setContentView(pad);
	}

	@Override
	public void removePad() {
		if (padView != null && pad != null) {

			padView.clearIndex();
			padView.clearPreviewContent();
			padView.clearTime();

			padView.getPlaylistLabel().textProperty().unbind();
			padView.getPlaylistLabel().setText("");

			padView.setTriggerLabelActive(false);

			padView.loopLabelVisibleProperty().unbind();

			pad.contentProperty().removeListener(padContentListener);
			pad.statusProperty().removeListener(padStatusListener);

			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				durationable.durationProperty().removeListener(padDurationListener);
				durationable.positionProperty().removeListener(padPositionListener);
			}
			pad.setController(null);

			// GUI Cleaning
			padPositionListener.stopWaning();
			padView.removeStyleClasses();
		}

		this.pad = null;
	}

	@Override
	public void handle(Event event) {
		if (event instanceof MouseEvent) {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
				MouseEvent mouseEvent = (MouseEvent) event;
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
					if (pad.getStatus() == PadStatus.PLAY) {
						onStop();
					} else {
						onPlay();
					}
				}
			}
		} else if (event instanceof TouchEvent) {
			if (pad.getStatus() == PadStatus.PLAY) {
				onStop();
			} else {
				onPlay();
			}
		}
	}

	private void onPlay() {
		if (pad.getContent() != null) {
			pad.setStatus(PadStatus.PLAY);
		}
	}

	private void onStop() {
		if (pad.getContent() != null) {
			pad.setStatus(PadStatus.STOP);
		}
	}

	@Override
	public void updateTimeLabel() {
		if (pad.getContent() != null && pad.getStatus() != PadStatus.EMPTY && pad.getStatus() != PadStatus.ERROR) {
			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();

				Duration duration = durationable.getDuration();
				Duration position = durationable.getPosition();

				if (duration != null) {
					// Nur Gesamtzeit anzeigen
					if (pad.getStatus() == PadStatus.READY || position == null) {
						String time = durationToString(duration);
						padView.setTime(time);
						padView.getPlayBar().setProgress(0);
						padView.setCueInProgress(0);
					} else {
						// TODO: Duplicated code
						// Play/Gesamtzeit anzeigen
						TimeMode timeMode = pad.getPadSettings().getTimeMode();

						if (timeMode == TimeMode.REST) {
							Duration leftTime = duration.subtract(position);
							padView.setTime("- " + durationToString(leftTime));
						} else if (timeMode == TimeMode.PLAYED) {
							padView.setTime(durationToString(position));
						} else if (timeMode == TimeMode.BOTH) {
							String time = durationToString(position);
							String totalTime = durationToString(duration);
							padView.setTime(time + "/" + totalTime);
						}
					}
				}
				return;
			}
		}
		padView.getPlayBar().setProgress(0);
		padView.setTime(null);
	}

	private String durationToString(Duration value) {
		if (value != null) {
			int seconds = (int) ((value.toMillis() / 1000) % 60);
			int minutes = (int) ((value.toMillis() / (1000 * 60)) % 60);
			return String.format(DURATION_FORMAT, minutes, seconds);
		} else {
			return null;
		}
	}

	@Override
	public void updateButtonDisable() {
		// Not needed in touch mode
	}

	@Override
	public IPadPositionListener getPadPositionListener() {
		return padPositionListener;
	}

	@Override
	public ChangeListener<Duration> getPadDurationListener() {
		return padDurationListener;
	}
}
