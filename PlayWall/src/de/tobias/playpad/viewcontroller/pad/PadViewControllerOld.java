package de.tobias.playpad.viewcontroller.pad;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.dialog.ExceptionDialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.listener.MidiHandler;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.Pad.PadStatus;
import de.tobias.playpad.pad.Pad.TimeMode;
import de.tobias.playpad.model.PadException;
import de.tobias.playpad.model.PadException.PadExceptionType;
import de.tobias.playpad.model.Project;
import de.tobias.playpad.plugin.ExtensionHandler;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.PlayPadPlugin;
import de.tobias.playpad.plugin.viewcontroller.IPadViewController;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.view.AudioPadView;
import de.tobias.playpad.viewcontroller.PadSettingsViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.Refreshable;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PadViewController implements IPadViewController, EventHandler<ActionEvent>, ChangeListener<Duration> {

	private static final String DURATION_FORMAT = "%d:%02d";

	private static final String OPEN_FOLDER = "openFolder";

	public static ObservableList<PadException> exceptions = FXCollections.observableArrayList();

	private AudioPadView view;

	protected Pad pad;
	protected int page;

	// Listener
	private ChangeListener<Duration> totalDurationListener;
	private ChangeListener<AudioHandler> audioHandlerListener;
	private ChangeListener<PadStatus> stateListener;
	private ChangeListener<Boolean> audioLoadedListener;
	private ChangeListener<String> titleListener;
	private ChangeListener<PadException> exceptionListener;
	private PlayDurationEventHandler durationEventHandler;

	// Drag and Drop
	private PadDragHandler<?> dragAndDrop;

	// Refresh in Window
	private Refreshable refreshable;
	private NotificationHandler notificationHandler;

	private MidiHandler handler;
	protected int midiKeyPressedForPlay;

	// Window Referenz
	private PadSettingsViewController padSettingsViewController;

	private static int playedPlayers;

	private synchronized static void addPlayer() {
		playedPlayers++;
	}

	private synchronized static void removePlayer() {
		if (playedPlayers > 0) {
			playedPlayers--;
		}
	}

	public synchronized static int getPlayedPlayers() {
		return playedPlayers;
	}

	public <T extends Refreshable & NotificationHandler> PadViewController(T t, Project project, MidiHandler handler) {
		view = new AudioPadView(this);
		dragAndDrop = new PadDragHandler<T>(pad, view, t);

		this.refreshable = t;
		this.notificationHandler = t;
		this.handler = handler;

		view.getErrorLabel().setOnMouseClicked(event ->
		{
			showError();
		});
	}

	public Pad getPad() {
		return pad;
	}

	public void setPad(Pad pad, int page) {
		if (this.pad != null) {
			this.pad.setController(null);
			cleanUp();
		}

		this.pad = pad;
		this.page = page;
		this.pad.setController(this);
		this.dragAndDrop.setPad(pad);

		view.addDefaultButton();
		preparePad();

		for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener()) {
			try {
				listener.onPadVisible(pad);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void preparePad() {
		// Display Pad Once
		view.getIndexLabel().setText(String.valueOf(pad.getIndex() + 1));

		view.getTimeLabel().setText("");
		// Binding und Listener
		totalDurationListener = (a, b, c) ->
		{
			if (c != null) {
				showPadDuration();
			} else {
				Platform.runLater(() -> view.getTimeLabel().setText(""));
			}
		};

		stateListener = (a, b, c) -> stateChange(b, c);

		audioLoadedListener = (a, b, c) ->
		{
			// TODO Init MIDI Page
		};

		audioHandlerListener = (a, b, c) ->
		{
			b.durationProperty(pad).removeListener(totalDurationListener);
			c.durationProperty(pad).addListener(totalDurationListener);

			b.loadedProperty().removeListener(audioLoadedListener);
			c.loadedProperty().addListener(audioLoadedListener);
		};

		titleListener = (a, b, c) ->
		{
			view.getNameLabel().setText(c);
		};

		exceptionListener = (a, b, c) ->
		{
			if (c != null && !exceptions.contains(c)) {
				view.setErrorLabelActive(true);
				exceptions.add(c);
			} else {
				view.setErrorLabelActive(false);
				exceptions.remove(b);
			}
		};

		// AudioHandler Listener
		pad.audioHandlerProperty().addListener(audioHandlerListener);

		pad.titleProperty().addListener(titleListener);
		view.getNameLabel().setText(pad.getTitle());

		// Listener für Gesamtzeit (Wenn neues Media File oder so)
		pad.getAudioHandler().durationProperty(pad).addListener(totalDurationListener);

		// Time label Clickable für Time Display Art
		view.getTimeLabel().setOnMouseClicked(event ->
		{
			TimeMode timeMode = Profile.currentProfile().getProfileSettings().getPlayerTimeDisplayMode();
			if (pad.isCustomTimeMode()) {
				timeMode = pad.getTimeMode().get();
			}

			if (timeMode == TimeMode.PLAYED) {
				pad.setTimeMode(TimeMode.REST);
			} else if (timeMode == TimeMode.REST) {
				pad.setTimeMode(TimeMode.BOTH);
			} else if (timeMode == TimeMode.BOTH) {
				pad.setTimeMode(TimeMode.PLAYED);
			}
		});

		view.setLoopLabelActive(pad.isLoop());

		// Für LaunchPad Feedback
		pad.getAudioHandler().loadedProperty().addListener(audioLoadedListener);

		// State des Pad -> UI Änderungen (wie Progressbar oder Button)
		pad.statusProperty().addListener(stateListener);

		// Init
		stateChange(null, pad.getStatus());
		if (pad.getAudioHandler().durationProperty(pad).isNotNull().get()) {
			showPadDuration();
		}

		// Errors
		pad.lastExceptionProperty().addListener(exceptionListener);
		view.setErrorLabelActive(pad.getLastException() != null);
		if (pad.getLastException() != null && !exceptions.contains(pad.getLastException())) {
			exceptions.add(pad.getLastException());
		}

		// CSS
		view.getStyleClass().addAll("pad", "pad" + pad.getIndex());

		view.getIndexLabel().getStyleClass().addAll("pad-index", "pad" + pad.getIndex() + "-index", "pad-info",
				"pad" + pad.getIndex() + "-info");
		view.getTimeLabel().getStyleClass().addAll("pad-time", "pad" + pad.getIndex() + "-time", "pad-info", "pad" + pad.getIndex() + "-info");
		view.getNameLabel().getStyleClass().addAll("pad-title", "pad" + pad.getIndex() + "-title");

		view.getPlayBar().getStyleClass().addAll("pad-playbar", "pad" + pad.getIndex() + "-playbar");

		view.getPlayButton().getStyleClass().addAll("pad-button", "pad-playbutton", "pad" + pad.getIndex() + "-button",
				"pad" + pad.getIndex() + "-playbutton");
		view.getPauseButton().getStyleClass().addAll("pad-button", "pad-pausebutton", "pad" + pad.getIndex() + "-button",
				"pad" + pad.getIndex() + "-pausebutton");
		view.getStopButton().getStyleClass().addAll("pad-button", "pad-stopbutton", "pad" + pad.getIndex() + "-button",
				"pad" + pad.getIndex() + "-stopbutton");
		view.getNewButton().getStyleClass().addAll("pad-button", "pad-newbutton", "pad" + pad.getIndex() + "-button",
				"pad" + pad.getIndex() + "-newbutton");
		view.getSettingsButton().getStyleClass().addAll("pad-button", "pad-settingsbutton", "pad" + pad.getIndex() + "-button",
				"pad" + pad.getIndex() + "-settingsbutton");

		view.getPlayButton().getGraphic().getStyleClass().addAll("pad-button-icon", "pad-playbutton-icon",
				"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");
		view.getPauseButton().getGraphic().getStyleClass().addAll("pad-button-icon", "pad-pausebutton-icon",
				"pad" + pad.getIndex() + "-button-icon", "padv-playbutton--icon");
		view.getStopButton().getGraphic().getStyleClass().addAll("pad-button-icon", "pad-stopbutton-icon",
				"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");
		view.getNewButton().getGraphic().getStyleClass().addAll("pad-button-icon", "pad-newbutton-icon", "pad" + pad.getIndex() + "-button-icon",
				"pad" + pad.getIndex() + "-playbutton-icon");
		view.getSettingsButton().getGraphic().getStyleClass().addAll("pad-button-icon", "pad-deletebutton-icon",
				"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");

		view.getButtonBox().getStyleClass().add("pad-button-box");
		view.getRoot().getStyleClass().add("pad-root");
	}

	/*
	 * Button Action
	 */
	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == view.getPlayButton()) {
			pad.setStatus(PadStatus.PLAY);
		} else if (event.getSource() == view.getPauseButton()) {
			pad.setStatus(PadStatus.PAUSE);
		} else if (event.getSource() == view.getStopButton()) {
			pad.setStatus(PadStatus.STOP);
		} else if (event.getSource() == view.getSettingsButton()) {
			openSettings();
		} else if (event.getSource() == view.getNewButton()) {
			if (getPlayedPlayers() > 0) {
				notificationHandler.notify(Localization.getString(Strings.Error_Pad_Livemode), PlayPadMain.notificationDisplayTimeMillis);
			} else {
				chooseFile(event);
			}
		}
	}

	private void chooseFile(ActionEvent event) {
		FileChooser chooser = new FileChooser();

		// File Extension
		ExtensionFilter extensionFilter = new ExtensionFilter(Localization.getString(Strings.File_Filter_Media),
				AudioRegistry.geAudioType().getSupportedTypes());
		chooser.getExtensionFilters().add(extensionFilter);
		for (ExtensionHandler handler : PlayPadPlugin.getImplementation().getExtensionsHandler()) {
			chooser.getExtensionFilters().add(new ExtensionFilter(handler.getName(), handler.getExtensions()));
		}

		// Last Folder
		Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(OPEN_FOLDER);
		if (openFolder != null) {
			File folder = new File(openFolder.toString());
			chooser.setInitialDirectory(folder);
		}

		File file = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
		if (file != null) {
			Path path = file.toPath();
			ExtensionFilter filter = chooser.getSelectedExtensionFilter();

			for (ExtensionHandler handler : PlayPadPlugin.getImplementation().getExtensionsHandler()) {
				if (filter.getDescription().equals(handler.getName())) {
					handler.handle(path, pad);
					return;
				}
			}

			pad.setPath(path);
			ApplicationUtils.getApplication().getUserDefaults().setData(OPEN_FOLDER, path.getParent().toString());
		}
	}

	private void openSettings() {
		if (padSettingsViewController == null) {
			padSettingsViewController = new PadSettingsViewController(pad, this, view.getScene().getWindow());
			padSettingsViewController.getStage().showAndWait();
			refreshable.updateData();

			padSettingsViewController = null;
		} else if (padSettingsViewController.getStage().isShowing()) {
			padSettingsViewController.getStage().toFront();
		}
	}

	@Override
	public void changed(ObservableValue<? extends Duration> arg0, Duration arg1, Duration newValue) {
		// Progressbar (Prozente)
		if (pad != null) {
			double value = newValue.toMillis() / pad.getAudioHandler().getDuration(pad).toMillis();
			view.getPlayBar().setProgress(value);

			// Label (Restlaufzeit)
			TimeMode timeMode = Profile.currentProfile().getProfileSettings().getPlayerTimeDisplayMode();
			if (pad.isCustomTimeMode()) {
				timeMode = pad.getTimeMode().get();
			}
			if (timeMode == TimeMode.REST) {
				Duration leftTime = pad.getAudioHandler().getDuration(pad).subtract(newValue);
				view.getTimeLabel().setText("- " + durationToString(leftTime));
			} else if (timeMode == TimeMode.PLAYED) {
				view.getTimeLabel().setText(durationToString(newValue));
			} else if (timeMode == TimeMode.BOTH) {
				String time = durationToString(newValue);
				String totalTime = durationToString(pad.getAudioHandler().getDuration(pad));

				view.getTimeLabel().setText(time + "/" + totalTime);
			}
		}
	}

	private String durationToString(Duration value) {
		int secounds = (int) ((value.toMillis() / 1000) % 60);
		int minutes = (int) ((value.toMillis() / (1000 * 60)) % 60);
		String time = String.format(DURATION_FORMAT, minutes, secounds);
		return time;
	}

	private void showPadDuration() {
		Duration c = pad.getAudioHandler().getDuration(pad);
		if (c != null) {
			Platform.runLater(() -> view.getTimeLabel().setText(durationToString(c)));
		} else {
			Platform.runLater(() -> view.getTimeLabel().setText(""));
		}
	}

	// GUI Veränderung und Pad Aktionen wenn sich der Sate durch die Buttons oder durch MIDI ändert
	public void stateChange(PadStatus oldState, PadStatus newState) {
		Profile profile = Profile.currentProfile();
		if (pad.isCustomLayout()) {
			pad.getLayout(profile.getProfileSettings().getLayoutType()).ifPresent(layout -> layout.updatePadView(this, oldState, newState));
		} else {
			profile.currentLayout().updatePadView(this, oldState, newState);
		}

		switch (newState) {
		case PLAY:
			playState(oldState, newState);
			break;
		case PAUSE:
			pauseState(oldState, newState);
			break;
		case STOP:
			stopState(oldState, newState);
			break;
		case READY:
			readyState();
			break;
		case EMPTY:
			emptyState();
			break;
		}
	}

	private void playState(PadStatus oldState, PadStatus newState) {
		// PadListener
		boolean ignoreNoMedia = false;
		for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener()) {
			try {
				if (listener.ignoreNoMedia(pad)) {
					ignoreNoMedia = true;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!ignoreNoMedia) {
			if (!pad.getAudioHandler().isMediaLoaded(pad)) {
				pad.loadMedia(); // Try to Load wenn nicht geladen
				if (!pad.getAudioHandler().isMediaLoaded(pad)) {
					String file = pad.getFile();
					if (file != null)
						file = file.replace("%20", " ");
					notificationHandler
							.showError(Localization.getString(Strings.Error_Pad_BaseName + PadExceptionType.FILE_NOT_FOUND.toString(), file));
					pad.setState(PadStatus.READY);
					return;
				}
			}
		}

		// PadListener
		for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener())
			try {
				if (!listener.onPlay(pad))
					return;
			} catch (Exception e) {
				e.printStackTrace();
			}

		// Bei Pause werden die Listener nicht entfernt, bei Stop schon. bei Stop wird der Listener = null. Nur dann muss er neu
		// hinzugefügt werden
		if (durationEventHandler == null && pad.currentDurationProperty() != null) {
			durationEventHandler = new PlayDurationEventHandler(this);
			pad.currentDurationProperty().addListener(durationEventHandler);
			pad.currentDurationProperty().addListener(this);
		}
		if (durationEventHandler != null)
			durationEventHandler.send = false;

		// midi Feedback
		// TODO MIDI Send Feedback
		// try {
		// } catch (MidiUnavailableException | InvalidMidiDataException e) {
		// notificationHandler.showError(Localization.getString(Strings.Error_Midi_Send, e.getLocalizedMessage()));
		// e.printStackTrace();
		// }

		if (pad.getAudioHandler().isMediaLoaded(pad))
			pad.play();
		addPlayer();

		Platform.runLater(() ->
		{
			view.getPlayButton().setDisable(true);
			view.getPauseButton().setDisable(false);
			view.getStopButton().setDisable(false);
			view.pseudoClassState(PseudoClasses.PLAY_CALSS, true);
		});
	}

	private void pauseState(PadStatus oldState, PadStatus newState) {
		pad.pause(true, () ->
		{
			// Bei Fade Out Ende löschen
			view.pseudoClassState(PseudoClasses.FADE_CLASS, false);
			removePlayer();
			durationEventHandler.stopWaning();
		});

		Platform.runLater(() ->
		{
			view.getPlayButton().setDisable(false);
			view.getPauseButton().setDisable(true);
			view.getStopButton().setDisable(false);

			view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			view.pseudoClassState(PseudoClasses.WARN_CLASS, false);

			// Fade Out Style hintufügen
			if (!pad.isEof() && (pad.isCustomFade() && pad.getFade().get().getFadeOut().toMillis() > 0
					|| !pad.isCustomFade() && Profile.currentProfile().getProfileSettings().getFade().getFadeOut().toSeconds() > 0))
				view.pseudoClassState(PseudoClasses.FADE_CLASS, true);
		});
		sendFeedback(oldState, newState);
	}

	private void stopState(PadStatus oldState, PadStatus newState) {
		if (pad.currentDurationProperty() != null) {
			pad.currentDurationProperty().removeListener(this);
			if (durationEventHandler != null)
				pad.currentDurationProperty().removeListener(durationEventHandler);
		}

		// PadListener
		for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener())
			try {
				listener.onStop(pad);
			} catch (Exception e) {
				e.printStackTrace();
			}

		// TODO Init MIDI Page

		boolean fadeListener = true;
		for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener()) {
			if (!listener.allowFade(pad)) {
				fadeListener = false;
			}
		}

		final boolean fade = fadeListener;

		// UI Clean in State READY
		Platform.runLater(() ->
		{
			view.getPauseButton().setDisable(true);
			view.getStopButton().setDisable(true);

			if (durationEventHandler != null)
				durationEventHandler.stopWaning();
			durationEventHandler = null;

			view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			view.pseudoClassState(PseudoClasses.WARN_CLASS, false);

			if (oldState != PadStatus.PAUSE && fade) {
				if (!pad.isEof() && (pad.isCustomFade() && pad.getFade().get().getFadeOut().toMillis() > 0
						|| !pad.isCustomFade() && Profile.currentProfile().getProfileSettings().getFade().getFadeOut().toSeconds() > 0)) {
					view.pseudoClassState(PseudoClasses.FADE_CLASS, true);
				}
			}
		});

		pad.stop(oldState != PadStatus.PAUSE && fade, () ->
		{
			// Bei Ende von Fade Out wird CSS removed
			view.pseudoClassState(PseudoClasses.FADE_CLASS, false);
		});

		sendFeedback(oldState, newState);
	}

	private void readyState() {
		// Clean UI vom Play
		Platform.runLater(() ->
		{
			view.getPlayBar().setProgress(0.0);
			showPadDuration();

			view.getPlayButton().setDisable(false);
			view.getPauseButton().setDisable(true);
			view.getStopButton().setDisable(true);

			view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			view.pseudoClassState(PseudoClasses.FADE_CLASS, false);
			view.pseudoClassState(PseudoClasses.WARN_CLASS, false);

			// PadListener
			for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener())
				try {
					listener.onReady(pad);
				} catch (Exception e) {
					e.printStackTrace();
				}
		});

		removePlayer();
	}

	private void emptyState() {
		view.getPlayButton().setDisable(true);
		view.getPauseButton().setDisable(true);
		view.getStopButton().setDisable(true);
	}

	public void sendFeedback(PadStatus oldState, PadStatus newState) {
		// TODO MIDI Send Feedback
	}

	public void cleanUp() {
		if (pad != null) {
			// Wenn Play oder Pause dann erst Stop, bevor alles Aufgeräumt wird
			if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE)
				pad.setStatus(PadStatus.STOP);

			pad.stop(false, null);

			// Listener für Gesamtzeit weg
			// State des Pad -> UI Änderungen (wie Progressbar oder Button)
			pad.getAudioHandler().durationProperty(pad).removeListener(totalDurationListener);
			pad.stateProperty().removeListener(stateListener);
			pad.getAudioHandler().loadedProperty().removeListener(audioLoadedListener);
			pad.titleProperty().removeListener(titleListener);
			pad.lastExceptionProperty().removeListener(exceptionListener);
			pad.audioHandlerProperty().removeListener(audioHandlerListener);

			// view.getNameLabel().textProperty().unbind(); // Clear Title Label
			view.getTimeLabel().setText(""); // Clear Time Label
			view.getPlayBar().setProgress(0); // Zeit Leiste
			view.setErrorLabelActive(false); // Error Weg

			view.getStyleClass().removeAll("pad", "pad" + pad.getIndex());

			view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			view.pseudoClassState(PseudoClasses.FADE_CLASS, false);
			view.pseudoClassState(PseudoClasses.WARN_CLASS, false);

			view.getIndexLabel().getStyleClass().removeAll("pad-index", "pad" + pad.getIndex() + "-index", "pad-info",
					"pad" + pad.getIndex() + "-info");
			view.getTimeLabel().getStyleClass().removeAll("pad-time", "pad" + pad.getIndex() + "-time", "pad-info",
					"pad" + pad.getIndex() + "-info");
			view.getNameLabel().getStyleClass().removeAll("pad-title", "pad" + pad.getIndex() + "-title");
			view.getPlayBar().getStyleClass().removeAll("pad-playbar", "pad" + pad.getIndex() + "-playbar");

			view.getPlayButton().getStyleClass().removeAll("pad-button", "pad-playbutton", "pad" + pad.getIndex() + "-button",
					"pad" + pad.getIndex() + "-playbutton");
			view.getPauseButton().getStyleClass().removeAll("pad-button", "pad-pausebutton", "pad" + pad.getIndex() + "-button",
					"pad" + pad.getIndex() + "-pausebutton");
			view.getStopButton().getStyleClass().removeAll("pad-button", "pad-stopbutton", "pad" + pad.getIndex() + "-button",
					"pad" + pad.getIndex() + "-stopbutton");
			view.getNewButton().getStyleClass().removeAll("pad-button", "pad-newbutton", "pad" + pad.getIndex() + "-button",
					"pad" + pad.getIndex() + "-newbutton");
			view.getSettingsButton().getStyleClass().removeAll("pad-button", "pad-settingsbutton", "pad" + pad.getIndex() + "-button",
					"pad" + pad.getIndex() + "-settingsbutton");

			view.getPlayButton().getGraphic().getStyleClass().removeAll("pad-button-icon", "pad-playbutton-icon",
					"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");
			view.getPauseButton().getGraphic().getStyleClass().removeAll("pad-button-icon", "pad-pausebutton-icon",
					"pad" + pad.getIndex() + "-button-icon", "padv-playbutton--icon");
			view.getStopButton().getGraphic().getStyleClass().removeAll("pad-button-icon", "pad-stopbutton-icon",
					"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");
			view.getNewButton().getGraphic().getStyleClass().removeAll("pad-button-icon", "pad-newbutton-icon",
					"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");
			view.getSettingsButton().getGraphic().getStyleClass().removeAll("pad-button-icon", "pad-deletebutton-icon",
					"pad" + pad.getIndex() + "-button-icon", "pad" + pad.getIndex() + "-playbutton-icon");

			view.getButtonBox().getStyleClass().removeAll("pad-button-box");
			view.getRoot().getStyleClass().removeAll("pad-root");
		}
	}

	public AudioPadView getView() {
		return view;
	}

	// TODO ErrorHandling
	private void showError() {
		PadException lastException = pad.getLastException();

		try {
			ExceptionDialog dialog = new ExceptionDialog(lastException);
			dialog.setHeaderText(Localization.getString(Strings.Error_Pad_BaseName + lastException.getType().name(), pad.getPath()));

			if (PlayPadMain.stageIcon.isPresent()) {
				Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
				stage.getIcons().add(PlayPadMain.stageIcon.get());
			}

			dialog.setTitle(Localization.getString(Strings.UI_Dialog_Error_Title));
			dialog.initOwner(refreshable.getStage());
			dialog.showAndWait();

			view.setErrorLabelActive(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Fügt einen Button zum Pad hinzu
	 * 
	 * @param button
	 * 
	 * @since 2.0.2
	 */
	public void addButton(Button button, ButtonPosition buttonPosition) {
		if (buttonPosition == ButtonPosition.TOP) {
			if (!view.getButtonBox().getChildren().contains(button)) {
				view.getInfoBox().getChildren().add(button);
			}
		} else if (buttonPosition == ButtonPosition.BOTTOM) {
			if (!view.getButtonBox().getChildren().contains(button)) {
				view.getButtonBox().getChildren().add(button);
			}
		}
	}

	/**
	 * Fügt einen Button zum Pad hinzu
	 * 
	 * @param button
	 *            Button
	 * @param index
	 *            Index
	 * 
	 * @since 2.0.2
	 */
	public void addNode(Node button, int index, ButtonPosition buttonPosition) {
		if (buttonPosition == ButtonPosition.TOP) {
			if (!view.getButtonBox().getChildren().contains(button)) {
				view.getInfoBox().getChildren().add(index, button);
			}
		} else if (buttonPosition == ButtonPosition.BOTTOM) {
			if (!view.getButtonBox().getChildren().contains(button)) {
				view.getButtonBox().getChildren().add(index, button);
			}
		}
	}

	/**
	 * Löscht einen Button vom Pad
	 * 
	 * @param button
	 * 
	 * @since 2.0.2
	 */
	public void removeNode(Node button) {
		if (view.getButtonBox().getChildren().contains(button)) {
			view.getButtonBox().getChildren().remove(button);
		} else if (view.getInfoBox().getChildren().contains(button)) {
			view.getInfoBox().getChildren().remove(button);
		}
	}

	public List<Node> getCustomNodes() {
		List<Node> buttons = new ArrayList<>();
		for (Node node : view.getButtonBox().getChildren()) {
			if (node != view.getPlayButton() || node != view.getPauseButton() || node != view.getStopButton() || node != view.getNewButton()
					|| node != view.getSettingsButton()) {
				buttons.add((Button) node);
			}
		}
		return buttons;
	}

	public void showDnDLayout(boolean show) {
		view.pseudoClassState(PseudoClasses.DRAG_CLASS, show);
	}
}
