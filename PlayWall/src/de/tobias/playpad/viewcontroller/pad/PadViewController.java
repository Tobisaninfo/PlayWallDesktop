package de.tobias.playpad.viewcontroller.pad;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.pad.conntent.play.Durationable;
import de.tobias.playpad.pad.listener.PadContentListener;
import de.tobias.playpad.pad.listener.PadDurationListener;
import de.tobias.playpad.pad.listener.PadLockedListener;
import de.tobias.playpad.pad.listener.PadPositionListener;
import de.tobias.playpad.pad.listener.PadStatusListener;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.view.PadView;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.pad.PadSettingsViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PadViewController implements EventHandler<ActionEvent>, IPadViewController {

	private static final String DURATION_FORMAT = "%d:%02d";
	private static final String OPEN_FOLDER = "openFolder";

	private PadView view;
	private Pad pad;

	private PadLockedListener padLockedListener;
	private PadStatusListener padStatusListener;
	private PadContentListener padContentListener;
	private PadDurationListener padDurationListener;
	private PadPositionListener padPositionListener;

	private PadDragListener padDragListener;
	private transient PadSettingsViewController padSettingsViewController;

	public PadViewController() {
		view = new PadView(this);

		// TODO Disable this
		/*padLockedListener = new PadLockedListener(this);
		padStatusListener = new PadStatusListener(this);
		padContentListener = new PadContentListener(this);
		padDurationListener = new PadDurationListener(this);
		padPositionListener = new PadPositionListener(this);*/

		// Listener muss nur einmal hier hinzugefügt werden, weil bei einem neuen Profile, werden neue PadViewController erzeugt
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		profileSettings.lockedProperty().addListener(padLockedListener);
	}

	@Override
	public IPadView getParent() {
		return view;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == view.getPlayButton()) {
			onPlay();
		} else if (event.getSource() == view.getPauseButton()) {
			onPause();
		} else if (event.getSource() == view.getStopButton()) {
			onStop();
		} else if (event.getSource() == view.getNewButton()) {
			onNew(event);
		} else if (event.getSource() == view.getSettingsButton()) {
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
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();
		if (pad.getProject() != null) {
			if (settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getPlayedPlayers() > 0) {
				PlayPadPlugin.getImplementation().getMainViewController().showLiveInfo();
				return;
			}
		}

		FileChooser chooser = new FileChooser();

		// File Extension
		ExtensionFilter extensionFilter = new ExtensionFilter(Localization.getString(Strings.File_Filter_Media),
				PadContentRegistry.getSupportedFileTypes());
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

			try {
				Set<PadContentConnect> connects = PadContentRegistry.getPadContentConnectsForFile(file.toPath());
				if (!connects.isEmpty()) {
					if (connects.size() > 1) {
						FileDragOptionView hud = new FileDragOptionView(view);
						hud.showDropOptions(connects, connect ->
						{
							if (connect != null) {
								setNewPadContent(file, path, connect);
								hud.hide();
							}
						});
					} else {
						PadContentConnect connect = connects.iterator().next();
						setNewPadContent(file, path, connect);
					}
				}
			} catch (UnkownPadContentException e) {
				e.printStackTrace();
			}

			ApplicationUtils.getApplication().getUserDefaults().setData(OPEN_FOLDER, path.getParent().toString());
		}
	}

	private void setNewPadContent(File file, Path path, PadContentConnect connect) {
		PadContent content = pad.getContent();
		if (pad.getContent() == null || !pad.getContent().getType().equals(connect.getType())) {
			content = connect.newInstance(pad);
			this.pad.setContent(content);
		}

		try {
			content.handlePath(file.toPath());
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
	}

	private void onSettings() {
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();
		IMainViewController mvc = PlayPadPlugin.getImplementation().getMainViewController();

		if (mvc != null) {
			if (pad.getProject() != null) {
				if (settings.isLiveMode() && settings.isLiveModeSettings() && pad.getProject().getPlayedPlayers() > 0) {
					mvc.showLiveInfo();
					return;
				}
			}

			Stage owner = mvc.getStage();
			if (padSettingsViewController == null) {
				padSettingsViewController = new PadSettingsViewController(pad, owner);
				padSettingsViewController.getStage().setOnHiding(ev ->
				{
					if (view != null && pad != null)
						view.setTriggerLabelActive(pad.hasTriggerItems());
				});
			}
			padSettingsViewController.getStage().show();
		}
	}

	@Override
	public void unconnectPad() {
		view.getIndexLabel().setText("");
		view.clearPreviewContent();
		view.getTimeLabel().setText("");

		view.setTriggerLabelActive(false);

		view.getLoopLabel().visibleProperty().unbind();

		if (pad != null) {
			pad.contentProperty().removeListener(padContentListener);
			pad.statusProperty().removeListener(padStatusListener);

			if (pad.getContent() instanceof Durationable) {
				Durationable durationable = (Durationable) pad.getContent();
				durationable.durationProperty().removeListener(padDurationListener);
				durationable.positionProperty().removeListener(padPositionListener);
			}
			pad.setController(null);
			padDragListener = null;

			// GUI Cleaning
			getPadPositionListener().stopWaning();
			view.removeStyleClasses(pad);
		}
		this.pad = null;
	}

	@Override
	public Pad getPad() {
		return pad;
	}

	@Override
	public void setPad(Pad pad) {
		unconnectPad();

		this.pad = pad;

		view.setPreviewContent(pad);
		view.addStyleClasses(pad);

		connectPad();
	}

	@Override
	public void connectPad() {
//		pad.setController(this); TODO

		try {
			// Settings
			view.getIndexLabel().setText(String.valueOf(pad.getIndexReadable()));
			view.getLoopLabel().visibleProperty().bind(pad.loopProperty());

			view.setTriggerLabelActive(pad.hasTriggerItems());

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

//			padDragListener = new PadDragListener(pad, view); TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
						view.getTimeLabel().setText(time);
						view.getPlayBar().setProgress(0);
					} else {
						// Play/Gesamtzeit anzeigen
						TimeMode timeMode = pad.getTimeMode();

						if (timeMode == TimeMode.REST) {
							Duration leftTime = duration.subtract(position);

							view.getTimeLabel().setText("- " + durationToString(leftTime));
						} else if (timeMode == TimeMode.PLAYED) {
							view.getTimeLabel().setText(durationToString(position));
						} else if (timeMode == TimeMode.BOTH) {
							String time = durationToString(position);
							String totalTime = durationToString(duration);

							view.getTimeLabel().setText(time + "/" + totalTime);
						}
					}
				}
				return;
			}
		}
		view.getPlayBar().setProgress(0);
		view.getTimeLabel().setText("");
	}

	public String durationToString(Duration value) {
		if (value != null) {
			int secounds = (int) ((value.toMillis() / 1000) % 60);
			int minutes = (int) ((value.toMillis() / (1000 * 60)) % 60);
			String time = String.format(DURATION_FORMAT, minutes, secounds);
			return time;
		} else {
			return null;
		}
	}

	public void updateButtonDisable() {
		if (pad == null) {
			return;
		}
		if (pad.getContent() != null) {
			if (pad.getStatus() == PadStatus.PLAY) {
				view.getPlayButton().setDisable(true);
				view.getPauseButton().setDisable(false);
				view.getStopButton().setDisable(false);
				view.getNewButton().setDisable(true);
				view.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.PAUSE) {
				view.getPlayButton().setDisable(false);
				view.getPauseButton().setDisable(true);
				view.getStopButton().setDisable(false);
				view.getNewButton().setDisable(true);
				view.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.STOP) {
				view.getPlayButton().setDisable(false);
				view.getPauseButton().setDisable(true);
				view.getStopButton().setDisable(true);
				view.getNewButton().setDisable(true);
				view.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.READY) {
				view.getPlayButton().setDisable(false);
				view.getPauseButton().setDisable(true);
				view.getStopButton().setDisable(true);
				view.getNewButton().setDisable(false);
				view.getSettingsButton().setDisable(false);
			} else if (pad.getStatus() == PadStatus.ERROR) {
				view.getPlayButton().setDisable(true);
				view.getPauseButton().setDisable(true);
				view.getStopButton().setDisable(true);
				view.getNewButton().setDisable(false);
				view.getSettingsButton().setDisable(false);
			}
		} else if (pad.getStatus() == PadStatus.EMPTY || pad.getStatus() == PadStatus.ERROR || pad.getContent() == null
				|| !pad.getContent().isPadLoaded()) {
			view.getPlayButton().setDisable(true);
			view.getPauseButton().setDisable(true);
			view.getStopButton().setDisable(true);
			view.getNewButton().setDisable(false);
			view.getSettingsButton().setDisable(false);
		}

		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			view.getNewButton().setDisable(true);
			view.getSettingsButton().setDisable(true);
		}
	}

	@Override
	public void showDnDLayout(boolean b) {
		view.pseudoClassState(PseudoClasses.DRAG_CLASS, b);
	}

	// getter for listener
	public PadDurationListener getPadDurationListener() {
		return padDurationListener;
	}

	public PadStatusListener getPadStatusListener() {
		return padStatusListener;
	}

	public PadPositionListener getPadPositionListener() {
		return padPositionListener;
	}

	public PadDragListener getPadDragListener() {
		return padDragListener;
	}
}
