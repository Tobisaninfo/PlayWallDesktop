package de.tobias.playpad.layout.desktop.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.DesktopEditMode;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutFactory;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.listener.*;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.pad.PadSettingsViewController;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public class DesktopPadViewController implements IPadViewController, EventHandler<ActionEvent> {

	public static final String OPEN_FOLDER = "openFolder";
	private static final String DURATION_FORMAT = "%d:%02d";

	private DesktopPadView padView;
	private Pad pad;

	private PadLockedListener padLockedListener;
	private PadStatusListener padStatusListener;
	private PadContentListener padContentListener;
	private PadDurationListener padDurationListener;
	private IPadPositionListener padPositionListener;

	private DesktopPadDragListener padDragListener;

	private static DesktopMainLayoutFactory connect;

	DesktopPadViewController(DesktopPadView padView, DesktopMainLayoutFactory connect) {
		this.padView = padView;

		if (DesktopPadViewController.connect != connect) // Set once
			DesktopPadViewController.connect = connect;

		padLockedListener = new PadLockedListener(this);
		padStatusListener = new PadStatusListener(this);
		padContentListener = new PadContentListener(this);
		padDurationListener = new PadDurationListener(this);
		padPositionListener = new PadPositionListener(this);

		// Listener muss nur einmal hier hinzugefügt werden, weil bei einem neuen Profile, werden neue PadViewController
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

			// Update Listener
			padContentListener.setPad(pad);
			padPositionListener.setPad(pad);

			// Add Listener
			pad.contentProperty().addListener(padContentListener);
			pad.statusProperty().addListener(padStatusListener);

			// Inital Listener call with new data
			padContentListener.changed(null, null, pad.getContent()); // Add Duration listener
			padStatusListener.changed(null, null, pad.getStatus());

			// Add Drag and Drop Listener
			padDragListener = new DesktopPadDragListener(pad, padView, connect);
			padDragListener.addListener();
		} catch (Exception e) {
			Logger.error(e);
		}

		padView.applyStyleClasses(pad.getPadIndex());
		padView.setContentView(pad);
	}

	@Override
	public void removePad() {
		if (padView != null && pad != null) {
			// Clear VIew
			padView.clearIndexLabel();
			padView.clearPreviewContentView();
			padView.clearTimeLabel();
			padView.setTriggerLabelActive(false);
			padView.loopLabelVisibleProperty().unbind();

			// Remove Bindings & Listener
			pad.contentProperty().removeListener(padContentListener);
			pad.statusProperty().removeListener(padStatusListener);

			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				durationable.durationProperty().removeListener(padDurationListener);
				durationable.positionProperty().removeListener(padPositionListener);
			}
			pad.setController(null);

			padDragListener.removeListener();
			padDragListener = null;

			// GUI Cleaning
			padPositionListener.stopWaning();
			padView.removeStyleClasses();
		}

		this.padDragListener = null;
		this.pad = null;

		// Hide Loading Animation
		if (getView() != null)
			getView().showBusyView(false);

	}

	@Override
	public void handle(ActionEvent event) {
		if (connect.getEditMode() == DesktopEditMode.PLAY) {
			if (event.getSource() == padView.getPlayButton()) {
				onPlay();
			} else if (event.getSource() == padView.getPauseButton()) {
				onPause();
			} else if (event.getSource() == padView.getStopButton()) {
				onStop();
			} else if (event.getSource() == padView.getNewButton()) {
				try {
					onNew(event);
				} catch (NoSuchComponentException e) {
					// TODO Error Handling
					Logger.error(e);
				}
			} else if (event.getSource() == padView.getSettingsButton()) {
				onSettings();
			}
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

	private void onNew(ActionEvent event) throws NoSuchComponentException {
		GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (pad.getProject() != null) {
			if (settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getActivePlayers() > 0) {
				return;
			}
		}

		FileChooser chooser = new FileChooser();
		PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();

		// File Extension
		ExtensionFilter extensionFilter = new ExtensionFilter(Localization.getString(Strings.File_Filter_Media),
				registry.getSupportedFileTypes());
		chooser.getExtensionFilters().add(extensionFilter);

		// Last Folder
		Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(OPEN_FOLDER);
		if (openFolder != null) {
			File folder = new File(openFolder.toString());
			chooser.setInitialDirectory(folder);
		}

		File file = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
		if (file != null) {
			Path path = file.toPath();

			Set<PadContentFactory> connects = registry.getPadContentConnectsForFile(file.toPath());
			if (!connects.isEmpty()) {
				if (connects.size() > 1) { // Multiple content types possible
					FileDragOptionView hud = new FileDragOptionView(padView.getRootNode());
					hud.showDropOptions(connects, connect ->
					{
						if (connect != null) {
							setNewPadContent(path, connect);
							hud.hide();
						}
					});
				} else {
					PadContentFactory connect = connects.iterator().next();
					setNewPadContent(path, connect);
				}
			}

			ApplicationUtils.getApplication().getUserDefaults().setData(OPEN_FOLDER, path.getParent().toString());
		}
	}

	private void setNewPadContent(Path path, PadContentFactory connect) {
		if (pad.getContent() == null || !pad.getContent().getType().equals(connect.getType())) {
			this.pad.setContentType(connect.getType());
		}

		if (pad.isPadVisible()) {
			pad.getController().getView().showBusyView(true);
		}

		pad.setPath(path);
	}

	private void onSettings() {
		GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
		IMainViewController mvc = PlayPadPlugin.getInstance().getMainViewController();

		if (mvc != null) {
			if (pad.getProject() != null) {
				if (settings.isLiveMode() && settings.isLiveModeSettings() && pad.getProject().getActivePlayers() > 0) {
					return;
				}
			}

			Stage owner = mvc.getStage();

			PadSettingsViewController padSettingsViewController = new PadSettingsViewController(pad, owner);
			padSettingsViewController.getStageContainer().ifPresent(nvcStage -> nvcStage.addCloseHook(() -> {
				if (padView != null && pad != null)
					padView.setTriggerLabelActive(pad.getPadSettings().hasTriggerItems());
				return true;
			}));
			padSettingsViewController.getStageContainer().ifPresent(NVCStage::show);
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
						padView.setPlayBarProgress(0);
						padView.setCueInProgress(0);
					} else {
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
		padView.setPlayBarProgress(0);
		padView.setCueInProgress(0);
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
		if (pad == null) {
			return;
		}
		if (pad.getContent() != null) {
			padView.addDefaultElements(pad);

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
			} else if (pad.getStatus() == PadStatus.NOT_FOUND) {
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

		// Disable Settings and New wenn Locked
		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			padView.getNewButton().setDisable(true);
			padView.getSettingsButton().setDisable(true);
		}

		// Alles Desktivieren, wenn nicht Play Mode
		if (connect.getEditMode() != DesktopEditMode.PLAY) {
			padView.getPlayButton().setDisable(true);
			padView.getPauseButton().setDisable(true);
			padView.getStopButton().setDisable(true);
			padView.getNewButton().setDisable(true);
			padView.getSettingsButton().setDisable(true);
		}
	}

	@Override
	public IPadPositionListener getPadPositionListener() {
		return padPositionListener;
	}

	@Override
	public ChangeListener<Duration> getPadDurationListener() {
		return padDurationListener;
	}

	public DesktopPadDragListener getPadDragListener() {
		return padDragListener;
	}
}
