package de.tobias.playpad.layout.touch;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadContentRegistry;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.play.Durationable;
import de.tobias.playpad.pad.listener.IPadPositionListener;
import de.tobias.playpad.pad.listener.PadContentListener;
import de.tobias.playpad.pad.listener.PadDurationListener;
import de.tobias.playpad.pad.listener.PadLockedListener;
import de.tobias.playpad.pad.listener.PadPositionListener;
import de.tobias.playpad.pad.listener.PadStatusListener;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.pad.PadSettingsViewController;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TouchPadViewController implements IPadViewControllerV2, EventHandler<Event> {

	protected static final String CURRENT_PAGE_BUTTON = "current-page-button";
	private static final String OPEN_FOLDER = "openFolder";
	private static final String DURATION_FORMAT = "%d:%02d";

	private TouchPadView padView;
	private Pad pad;

	private PadLockedListener padLockedListener;
	private PadStatusListener padStatusListener;
	private PadContentListener padContentListener;
	private PadDurationListener padDurationListener;
	private IPadPositionListener padPositionListener;

	private PadDragListener padDragListener;
	private transient PadSettingsViewController padSettingsViewController;

	public TouchPadViewController(TouchPadView padView) {
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
		if (padView != null && pad != null) {

			padView.clearIndex();
			padView.clearPreviewContent();
			padView.clearTime();

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
			padDragListener = null;

			// GUI Cleaning
			padPositionListener.stopWaning();
			padView.removeStyleClasses();
		}

		this.padDragListener = null;
		this.pad = null;
	}

	@Override
	public void handle(Event event) {
		if (event instanceof MouseEvent) {
			if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
				MouseEvent mouseEvent = (MouseEvent) event;
				if (mouseEvent.getButton() == MouseButton.PRIMARY || mouseEvent.getButton() == MouseButton.SECONDARY) {
					handleAction(mouseEvent.getButton() == MouseButton.PRIMARY, event);
				}
			}

		}
	}

	/**
	 * Diese Methode handlet die Eingabe für Touch oder Mouse
	 * 
	 * @param primaryAction
	 *            <code>true</code> Touch einfacher Click, Mause Primary Button
	 * @param event
	 *            event
	 */
	private void handleAction(boolean primaryAction, Event event) {
		if (primaryAction) {
			if (pad.getStatus() == PadStatus.EMPTY) {
				try {
					onNew(event);
				} catch (NoSuchComponentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (pad.getStatus() == PadStatus.PLAY) {
				onStop();
			} else {
				onPlay();
			}
		} else {
			onSettings();
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

	private void onNew(Event event) throws NoSuchComponentException {
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();
		if (pad.getProject() != null) {
			if (settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getPlayedPlayers() > 0) {
				PlayPadPlugin.getImplementation().getMainViewController().showLiveInfo();
				return;
			}
		}

		FileChooser chooser = new FileChooser();
		PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();

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

			Set<PadContentConnect> connects = registry.getPadContentConnectsForFile(file.toPath());
			if (!connects.isEmpty()) {
				if (connects.size() > 1) {
					FileDragOptionView hud = new FileDragOptionView(padView.getRootNode());
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
					if (padView != null && pad != null)
						padView.setTriggerLabelActive(pad.hasTriggerItems());
				});
			}
			padSettingsViewController.getStage().show();
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
					} else {
						// Play/Gesamtzeit anzeigen
						TimeMode timeMode = pad.getTimeMode();

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

	public PadDragListener getPadDragListener() {
		return padDragListener;
	}
}
