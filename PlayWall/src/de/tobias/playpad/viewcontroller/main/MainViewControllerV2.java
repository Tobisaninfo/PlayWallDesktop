package de.tobias.playpad.viewcontroller.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.MidiUnavailableException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.mapper.listener.KeyboardHandler;
import de.tobias.playpad.action.mapper.listener.MidiHandler;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutConnect;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.playpad.viewcontroller.dialog.ErrorSummaryDialog;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import de.tobias.utils.ui.BasicControllerSettings;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainViewControllerV2 extends ViewController implements IMainViewController, NotificationHandler, ProfileListener {

	private static final int FIRST_PAGE = 0;

	@FXML private VBox headerBox;
	@FXML private GridPane padGridPane;

	@FXML private AnchorPane gridContainer;
	private NotificationPane notificationPane;

	private List<IPadViewV2> padViews;

	private MenuToolbarViewController menuToolbarViewController;

	private Project openProject;
	private int currentPageShowing = -1;

	// Mapper
	private Midi midi;
	private MidiHandler midiHandler;
	private KeyboardHandler keyboardHandler;

	// Style
	private Color gridColor;

	// Layout
	private MainLayoutConnect mainLayout;
	private List<MainLayoutHandler> layoutActions;

	// Listener
	private VolumeChangeListener volumeChangeListener;
	private LockedListener lockedListener;
	private LayoutChangedListener layoutChangedListener;

	public MainViewControllerV2(List<WindowListener<IMainViewController>> listener) {
		super("mainViewV2", "de/tobias/playpad/assets/view/main/", null, PlayPadMain.getUiResourceBundle());
		padViews = new ArrayList<>();

		// Init ErrorSummaryViewController
		new ErrorSummaryDialog(getStage()); // Instance in ErrorSummaryViewController.getInstance()

		// Layout Init
		layoutActions = new ArrayList<>();

		// Init Listener
		volumeChangeListener = new VolumeChangeListener(this);
		lockedListener = new LockedListener(this);
		layoutChangedListener = new LayoutChangedListener();

		setMainLayout(new DesktopMainLayoutConnect()); // DEBUG
		initMapper(openProject);

		Profile.registerListener(this);
		reloadSettings(null, Profile.currentProfile());

		// Wenn sich die Toolbar ändert werden die Button neu erstellt. Das ist hier, weil es nur einmal als Listener da
		// sein muss. Die Methode wird aber an unterschiedlichen stellen mehrmals aufgerufen
		performLayoutDependendAction((oldToolbar, newToolbar) ->
		{
			if (menuToolbarViewController != null)
				menuToolbarViewController.initPageButtons();
		});

		/*
		 * Gridline Color
		 */
		try {
			Field field = padGridPane.getClass().getDeclaredField("gridLines");
			field.setAccessible(true);
			Group group = (Group) field.get(padGridPane);
			if (group != null) {
				group.getChildren().addListener((javafx.collections.ListChangeListener.Change<? extends Node> c) ->
				{
					for (Node node : group.getChildren()) {
						if (node instanceof Line) {
							((Line) node).setStroke(gridColor);
						}
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Plugin Listener
		listener.forEach(l ->
		{
			try {
				l.onInit(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void initMapper(Project project) {
		/*
		 * Mapper Setup & Listener
		 */
		this.midi = Midi.getInstance();
		this.midiHandler = new MidiHandler(midi, this, project);
		this.midi.setListener(midiHandler);
		this.keyboardHandler = new KeyboardHandler(project, this);

	}

	@Override
	public void init() {
		padGridPane.getStyleClass().add("pad-grid");

		notificationPane = new NotificationPane(padGridPane);
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);
	}

	// main layout
	public MainLayoutConnect getMainLayout() {
		return mainLayout;
	}

	public void setMainLayout(MainLayoutConnect mainLayoutConnect) {
		removePadsFromView();
		removePadViews();

		this.mainLayout = mainLayoutConnect;
		initMainLayout();
	}

	private void initMainLayout() {
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		// Entfernt Volume listener
		if (menuToolbarViewController != null) {
			menuToolbarViewController.deinit();

			menuToolbarViewController.getVolumeSlider().valueProperty().unbindBidirectional(settings.volumeProperty());
			menuToolbarViewController.getVolumeSlider().valueProperty().removeListener(volumeChangeListener);
		}

		// Erstellt Neue Toolbar
		headerBox.getChildren().clear();
		MenuToolbarViewController newMenuToolbarViewController = mainLayout.createMenuToolbar(this);
		headerBox.getChildren().add(newMenuToolbarViewController.getParent());

		// Führt alle Listener für diesen neuen Controller aus, damit Buttons und co wieder erstellt werden können
		layoutChangedListener.handle(layoutActions, this.menuToolbarViewController, newMenuToolbarViewController);
		this.menuToolbarViewController = newMenuToolbarViewController;

		menuToolbarViewController.setOpenProject(openProject);
		// Neue Volume listener
		menuToolbarViewController.getVolumeSlider().valueProperty().bindBidirectional(settings.volumeProperty());
		menuToolbarViewController.getVolumeSlider().valueProperty().addListener(volumeChangeListener);

		// Keyboard Shortcuts
		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
		menuToolbarViewController.loadKeybinding(globalSettings.getKeyCollection());

		// Update Locked Listener
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		lockedListener.changed(profileSettings.lockedProperty(), !profileSettings.isLocked(), profileSettings.isLocked());

		// Zeigt aktuelle Daten an
		createPadViews();
		showPage(currentPageShowing);
		loadUserCss();
	}

	// Stage Handling
	@Override
	public void initStage(Stage stage) {
		stage.fullScreenProperty().addListener((a, b, c) ->
		{
			if (Profile.currentProfile() != null)
				stage.setAlwaysOnTop(Profile.currentProfile().getProfileSettings().isWindowAlwaysOnTop());
		});

		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);
		stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination(KeyCombination.SHIFT_DOWN + "+Esc"));
		stage.setTitle(Localization.getString(Strings.UI_Window_Main_Title));
		stage.show();
	}

	@Override
	protected void loadSettings(BasicControllerSettings settings) {
		// Lädt die vorherigen Screen Positionen des Fenster
		List<Screen> screens = Screen.getScreensForRectangle(settings.getUserInfoAsDouble("x"), settings.getUserInfoAsDouble("y"),
				settings.width, settings.height);
		if (!screens.isEmpty()) {
			getStage().setX(settings.getUserInfoAsDouble("x"));
			getStage().setY(settings.getUserInfoAsDouble("y"));
		}

		getStage().setWidth(settings.width);
		getStage().setHeight(settings.height);
	}

	@Override
	protected void save(BasicControllerSettings settings) {
		// Speichert die aktuelle Position des Fensters
		settings.addUserInfo("x", getStage().getX());
		settings.addUserInfo("y", getStage().getY());
		settings.width = getStage().getWidth();
		settings.height = getStage().getHeight();
	}

	@Override
	public boolean closeRequest() {
		ErrorSummaryDialog.getInstance().getStage().close();

		if (Profile.currentProfile() != null) {
			ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

			// Frag den Nutzer ob das Programm wirdklich geschlossen werden sol
			// wenn ein Pad noch im Status Play ist
			if (openProject.getPlayedPlayers() > 0 && globalSettings.isLiveMode()) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setContentText(Localization.getString(Strings.UI_Window_Main_CloseRequest));

				alert.initOwner(getStage());
				alert.initModality(Modality.WINDOW_MODAL);
				Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
				PlayPadMain.stageIcon.ifPresent(alertStage.getIcons()::add);

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent())
					if (result.get() != ButtonType.OK)
						return false;
			}

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setContentText(Localization.getString(Strings.UI_Window_Main_SaveRequest));
			alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);

			Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
			yesButton.defaultButtonProperty().bind(yesButton.focusedProperty());

			Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
			noButton.defaultButtonProperty().bind(noButton.focusedProperty());

			Button cancelButton = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
			cancelButton.defaultButtonProperty().bind(cancelButton.focusedProperty());

			alert.initOwner(getStage());
			alert.initModality(Modality.WINDOW_MODAL);
			Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
			PlayPadMain.stageIcon.ifPresent(alertStage.getIcons()::add);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent()) {
				ButtonType buttonType = result.get();
				if (buttonType == ButtonType.YES) {
					// Projekt Speichern
					try {
						if (openProject.getRef() != null) {
							openProject.save();
							System.out.println("Saved Project: " + openProject);
						}
					} catch (Exception e) {
						e.printStackTrace();
						showErrorMessage(Localization.getString(Strings.Error_Project_Save));
					}
				} else if (buttonType == ButtonType.CANCEL) {
					return false;
				}
			}

			// Save Config - Its unabhängig vom Dialog, da es auch an anderen Stellen schon gespeichert wird
			try {
				if (Profile.currentProfile() != null)
					Profile.currentProfile().save();
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Profile_Save));
			}

			// Mapper Clear Feedback
			Profile.currentProfile().getMappings().getActiveMapping().clearFeedback();

			// MIDI Shutdown
			// Der schließt MIDI, da er es auch öffnet und verantwortlich ist
			if (profilSettings.isMidiActive()) {
				try {
					midi.close();
				} catch (MidiUnavailableException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (getStage().isIconified()) {
			getStage().setIconified(false);
		}

		// Verbindung von Pad und PadView wird getrennt. Zudem wird bei PLAY
		// oder PAUSE auf STOP gesetzt
		removePadsFromView();

		saveSettings();
		return true;
	}

	// project
	/**
	 * Öffnet ein Project. Das akutelle project ist in PlayPadImpl gespeichert.
	 * 
	 * @param project
	 *            neues Project
	 */
	public void openProject(Project project) {
		removePadsFromView();

		if (project != null)
			removePadsFromView();

		openProject = project;

		midiHandler.setProject(project);
		keyboardHandler.setProject(project);

		midiHandler.setProject(project);
		keyboardHandler.setProject(project);
		PadDragListener.setProject(project);
		ErrorSummaryDialog.getInstance().setProject(openProject);

		menuToolbarViewController.setOpenProject(openProject);

		createPadViews();
		showPage(FIRST_PAGE);
		loadUserCss();
		updateWindowTitle();
	}

	// Pad, Pages
	@Override
	public void createPadViews() {
		if (openProject == null) {
			return;
		}
		ProjectSettings projectSettings = openProject.getSettings();

		// Table
		padGridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / (double) projectSettings.getColumns();
		for (int i = 0; i < projectSettings.getColumns(); i++) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			padGridPane.getColumnConstraints().add(c);
		}

		padGridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / (double) projectSettings.getRows();
		for (int i = 0; i < projectSettings.getRows(); i++) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight(yPercentage * 100);
			padGridPane.getRowConstraints().add(c);
		}

		// Pads - Remove alte PadViews, falls noch welche vorhanden
		if (!padViews.isEmpty())
			removePadViews();

		// Neue PadViews
		for (int y = 0; y < projectSettings.getRows(); y++) {
			for (int x = 0; x < projectSettings.getColumns(); x++) {
				IPadViewV2 padView = mainLayout.createPadView();
				padGridPane.add(padView.getRootNode(), x, y);
				padViews.add(padView);
			}
		}

		// Min Size of window
		GlobalDesign currentLayout = Profile.currentProfile().currentLayout();
		double minWidth = currentLayout.getMinWidth(projectSettings.getColumns());
		double minHeight = currentLayout.getMinHeight(projectSettings.getRows());

		getStage().setMinWidth(minWidth);
		if (OS.getType() == OSType.MacOSX) {
			getStage().setMinHeight(minHeight + 100);
		} else {
			getStage().setMinHeight(minHeight + 150);
		}

		menuToolbarViewController.initPageButtons();
	}

	private void removePadViews() {
		padViews.forEach(view ->
		{
			padGridPane.getChildren().remove(view.getRootNode());
			mainLayout.recyclePadView(view);
		});
		padViews.clear();
	}

	/**
	 * Zeigt die aktuellen Pads von einem Profil zu einer Seite in den entsprechenden Views an.
	 */
	private void addPadsToView() {
		ProjectSettings settings = openProject.getSettings();

		int index = currentPageShowing * settings.getRows() * settings.getColumns();
		for (int i = 0; i < settings.getRows() * settings.getColumns(); i++) {
			if (padViews.size() > i) {
				IPadViewV2 view = padViews.get(i);
				Pad pad = openProject.getPad(index);

				view.getViewController().setupPad(pad);
			}
			index++;
		}
	}

	/**
	 * Entfernt alle Pads auf den Views.
	 */
	private void removePadsFromView() {
		// Clean old pads
		for (IPadViewV2 padView : padViews) {
			padView.getViewController().removePad();
		}
	}

	@Override
	public boolean showPage(int page) {
		if (openProject == null) {
			return false;
		}
		ProjectSettings projectSettings = openProject.getSettings();

		if (page < 0 || page >= projectSettings.getPageCount()) {
			return false;
		}

		// Clean
		removePadsFromView();
		this.currentPageShowing = page;
		addPadsToView();

		if (menuToolbarViewController != null) {
			menuToolbarViewController.highlightPageButton(page);
		}
		return true;
	}

	@Override
	public int getPage() {
		return currentPageShowing;
	}

	@Override
	public void setGlobalVolume(double volume) {
		if (openProject != null) {
			for (Pad pad : openProject.getPads().values()) {
				if (pad != null)
					pad.setMasterVolume(volume);
			}
		}
	}

	private boolean shown = false;

	@Override
	public void showLiveInfo() {
		if (!shown && menuToolbarViewController != null) {
			menuToolbarViewController.showLiveInfo(true);
			shown = true;
			Worker.runLater(() ->
			{
				try {
					Thread.sleep(PlayPadMain.displayTimeMillis * 2);
				} catch (Exception e) {
				}
				Platform.runLater(() ->
				{
					if (menuToolbarViewController != null)
						menuToolbarViewController.showLiveInfo(false);
					shown = false;
				});
			});
		}
	}

	// Settings
	@Override
	public void reloadSettings(Profile old, Profile currentProfile) {
		createPadViews();

		final DoubleProperty volumeFaderValueProperty = menuToolbarViewController.getVolumeSlider().valueProperty();

		if (old != null) {
			// Unbind Volume Slider
			volumeFaderValueProperty.unbindBidirectional(old.getProfileSettings().volumeProperty());
			volumeFaderValueProperty.removeListener(volumeChangeListener);

			// Clear Feedback on Devie (LaunchPad Light off)
			old.getMappings().getActiveMapping().getActions().forEach(action -> action.clearFeedback());

			// LockedListener
			old.getProfileSettings().lockedProperty().removeListener(lockedListener);
		}

		// Volume
		volumeFaderValueProperty.bindBidirectional(currentProfile.getProfileSettings().volumeProperty());
		volumeFaderValueProperty.addListener(volumeChangeListener);

		final ProfileSettings profileSettings = currentProfile.getProfileSettings();
		final Mapping activeMapping = currentProfile.getMappings().getActiveMapping();

		// LockedListener
		profileSettings.lockedProperty().addListener(lockedListener);

		// MIDI
		if (profileSettings.isMidiActive() && profileSettings.getMidiDevice() != null) {
			// Load known MIDI Device
			Worker.runLater(() ->
			{
				loadMidiDevice(profileSettings.getMidiDevice());
				Profile.currentProfile().getMappings().getActiveMapping().adjustPadColorToMapper(openProject);

				Platform.runLater(() ->
				{
					// Handle Mapper
					if (Profile.currentProfile() != null) {
						activeMapping.initFeedback();
						activeMapping.showFeedback(openProject);
					}
				});
			});
		}

		try {
			DefaultRegistry<MainLayoutConnect> registry = PlayPadPlugin.getRegistryCollection().getMainLayouts();
			MainLayoutConnect connect = registry.getComponent(currentProfile.getProfileSettings().getMainLayoutType());
			setMainLayout(connect);
		} catch (NoSuchComponentException e) {
			// TODO Error Handling
			e.printStackTrace();
		}

		loadUserCss();
		if (old != null && currentProfile != null) {
			showPage(currentPageShowing);
		}
	}

	@Override
	public void loadKeybinding(KeyCollection keys) {
		if (menuToolbarViewController != null) {
			menuToolbarViewController.loadKeybinding(keys);
		}
	}

	@Override
	public void setGridColor(Color color) {
		this.gridColor = color;
		try {
			Field field = padGridPane.getClass().getDeclaredField("gridLines");
			field.setAccessible(true);
			Group group = (Group) field.get(padGridPane);
			if (group != null) {
				for (Node node : group.getChildren()) {
					if (node instanceof Line) {
						((Line) node).setStroke(gridColor);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Notification
	@Override
	public void notify(String text, long duration) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration));
		}
	}

	@Override
	public void notify(String text, long duration, Runnable finish) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration, finish);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration, finish));
		}
	}

	@Override
	public void showError(String message) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showError(message);
		} else {
			Platform.runLater(() -> notificationPane.showError(message));
		}
	}

	@Override
	public void hide() {
		if (Platform.isFxApplicationThread()) {
			notificationPane.hide();
		} else {
			Platform.runLater(() -> notificationPane.hide());
		}
	}

	// Utils
	@Override
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener) {
		getParent().getScene().addEventHandler(eventType, listener);
	}

	@Override
	public void loadUserCss() {
		Scene scene = getStage().getScene();

		// Clear Old
		scene.getStylesheets().clear();

		// Layout Spezifisches CSS (Base)
		if (mainLayout.getStylesheet() != null) {
			scene.getStylesheets().add(mainLayout.getStylesheet());
		}

		// design spezific css
		if (openProject != null) {
			Profile.currentProfile().currentLayout().applyCssMainView(this, getStage(), openProject);
		}

		Profile.currentProfile().getMappings().getActiveMapping().adjustPadColorToMapper(openProject);
	}

	/**
	 * Init MIDI Device by using the Midi Class and show some feedback the user.
	 * 
	 * @param name
	 *            Device Name
	 * 
	 * @see Midi#lookupMidiDevice(String)
	 */
	private void loadMidiDevice(String name) {
		try {
			midi.lookupMidiDevice(name);
			notificationPane.showAndHide(Localization.getString(Strings.Info_Midi_Device_Connected, name), PlayPadMain.displayTimeMillis);
		} catch (NullPointerException e) {
			e.printStackTrace();
			showError(Localization.getString(Strings.Error_Midi_Device_Unavailible, name));
		} catch (IllegalArgumentException | MidiUnavailableException e) {
			showError(Localization.getString(Strings.Error_Midi_Device_Busy, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}

	public void updateWindowTitle() {
		if (openProject != null && Profile.currentProfile() != null) {
			getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title, openProject.getRef().getName(),
					Profile.currentProfile().getRef().getName()));
		} else {
			getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title));
		}
	}

	@Override
	public List<IPadViewV2> getPadViews() {
		return padViews;
	}

	@Override
	public MidiListener getMidiHandler() {
		return midiHandler;
	}

	@Override
	public MenuToolbarViewController getMenuToolbarController() {
		return menuToolbarViewController;
	}

	@Override
	public void performLayoutDependendAction(MainLayoutHandler runnable) {
		runnable.handle(null, menuToolbarViewController);
		layoutActions.add(runnable);
	}

	@Override
	public NotificationPane getNotificationPane() {
		return notificationPane;
	}
}
