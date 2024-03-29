package de.tobias.playpad.viewcontroller.main;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.device.CloseException;
import de.thecodelabs.midi.device.MidiDeviceInfo;
import de.thecodelabs.midi.event.KeyEventDispatcher;
import de.thecodelabs.midi.event.KeyEventType;
import de.thecodelabs.midi.mapping.KeyType;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.NotificationHandler;
import de.thecodelabs.utils.ui.scene.NotificationPane;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.thecodelabs.utils.util.OS.OSType;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.feedback.ColorAdjuster;
import de.tobias.playpad.design.ModernDesignSizeHelper;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileListener;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.playpad.viewcontroller.dialog.SaveDialog;
import de.tobias.playpad.viewcontroller.main.listener.LayoutChangedListener;
import de.tobias.playpad.viewcontroller.main.listener.LockedListener;
import de.tobias.playpad.viewcontroller.main.listener.VolumeChangeListener;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MainViewController extends NVC implements IMainViewController, NotificationHandler, ProfileListener {

	private static final int FIRST_PAGE = 0;

	@FXML
	private VBox headerBox;
	@FXML
	private GridPane padGridPane;

	@FXML
	private AnchorPane gridContainer;
	private NotificationPane notificationPane;

	private List<IPadView> padViews;

	private MenuToolbarViewController menuToolbarViewController;

	private Project openProject;
	private int currentPageShowing = 0;

	// Style
	private Color gridColor;

	// Layout
	private MainLayoutFactory mainLayout;
	private List<MainLayoutHandler> layoutActions;

	// Listener
	private VolumeChangeListener volumeChangeListener;
	private LockedListener lockedListener;
	private LayoutChangedListener layoutChangedListener;
	private ChangeListener<Number> notFoundListener;

	// Sync Listener
	private InvalidationListener projectTitleListener;
	private InvalidationListener pagesListener;

	private Thread autosaveThread;

	public MainViewController(Consumer<NVC> onFinish) {
		load("view/main", "MainView", Localization.getBundle(), e ->
		{
			NVCStage stage = e.applyViewControllerToStage();
			stage.addCloseHook(this::closeRequest);

			// Init with existing stage
			initKeyboardMapper();
			reloadSettings(null, Profile.currentProfile());
			onFinish.accept(e);

			// Min Size of window
			setMinSize();
			stage.show();
		});
	}

	private void setMinSize() {
		ProjectSettings projectSettings = openProject.getSettings();
		double minWidth = ModernDesignSizeHelper.getMinWidth(projectSettings.getColumns());
		double minHeight = ModernDesignSizeHelper.getMinHeight(projectSettings.getRows());

		if (minWidth < 500) {
			minWidth = 500;
		}

		getStage().setMinWidth(minWidth);
		if (OS.getType() == OSType.MacOSX) {
			getStage().setMinHeight(minHeight + 100);
		} else {
			getStage().setMinHeight(minHeight + 150);
		}
	}

	// Init
	@Override
	public void init() {
		padGridPane.getStyleClass().add("pad-grid");

		notificationPane = new NotificationPane(padGridPane);
		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);

		padViews = new ArrayList<>();

		// Layout Init
		layoutActions = new ArrayList<>();

		// Init Listener
		volumeChangeListener = new VolumeChangeListener(openProject);
		lockedListener = new LockedListener(this);
		layoutChangedListener = new LayoutChangedListener();
		notFoundListener = (observable, oldValue, newValue) -> {
			if (menuToolbarViewController != null)
				menuToolbarViewController.setNotFoundNumber(newValue.intValue());
		};

		// Sync Listener
		projectTitleListener = observable -> updateWindowTitle();
		pagesListener = observable -> {
			getMenuToolbarController().initPageButtons();
			showPage(0);
		};

		// Default Layout
		setMainLayout(PlayPadPlugin.getRegistries().getMainLayouts().getDefault());

		Profile.registerListener(this);

		// Wenn sich die Toolbar ändert werden die Button neu erstellt. Das ist hier, weil es nur einmal als Listener da
		// sein muss. Die Methode wird aber an unterschiedlichen stellen mehrmals aufgerufen
		performLayoutDependedAction((oldToolbar, newToolbar) ->
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
			Logger.error(e);
		}

		autosaveThread = new Thread(new AutosaveRunner(this));
		autosaveThread.start();
	}

	@Override
	public void initStage(Stage stage) {
		stage.fullScreenProperty().addListener((a, b, c) ->
		{
			if (Profile.currentProfile() != null)
				stage.setAlwaysOnTop(Profile.currentProfile().getProfileSettings().isWindowAlwaysOnTop());
		});

		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
		stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination(KeyCombination.SHIFT_DOWN + "+Esc"));
		stage.setTitle(Localization.getString(Strings.UI_WINDOW_MAIN_TITLE, "-", "-"));
		stage.show();
	}

	private void initKeyboardMapper() {
		registerKeyboardListener(KeyEvent.ANY, event -> {
			if (event.getTarget() instanceof AnchorPane) {
				if (!event.isShortcutDown()) {
					KeyCode code = null;
					KeyEventType type = null;

					if (event.getEventType() == KeyEvent.KEY_PRESSED) {
						code = event.getCode();
						type = KeyEventType.DOWN;

					} else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
						code = event.getCode();
						type = KeyEventType.UP;

					}

					// Only execute this, then the right event is triggered and this var is set
					if (code != null) {
						de.thecodelabs.midi.event.KeyEvent keyEvent = new de.thecodelabs.midi.event.KeyEvent(KeyType.KEYBOARD, type, code.ordinal());
						KeyEventDispatcher.dispatchEvent(keyEvent);
					}
				}
			}
		});

		// Request Focus for key listener
		getParent().requestFocus();

		getParent().getScene().focusOwnerProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				getParent().requestFocus();
			}
		});
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

		// Not Found Icon Update
		if (openProject != null)
			menuToolbarViewController.setNotFoundNumber(openProject.getNotFoundMedia());

		// Keyboard Shortcuts
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
		menuToolbarViewController.loadKeybinding(globalSettings.getKeyCollection());

		// Update Locked Listener
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		lockedListener.changed(profileSettings.lockedProperty(), !profileSettings.isLocked(), profileSettings.isLocked());

		// Zeigt aktuelle Daten an
		createPadViews();
		showPage(currentPageShowing);
		loadUserCss();
	}

	private boolean closeRequest() {
		if (Profile.currentProfile() != null) {
			ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
			GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();

			// Frag den Nutzer ob das Programm wirdklich geschlossen werden soll,
			// falls noch ein Pad im Status Play ist
			if (openProject.getActivePlayers() > 0 && globalSettings.isLiveMode()) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setContentText(Localization.getString(Strings.UI_WINDOW_MAIN_CLOSE_REQUEST));

				alert.initOwner(getStage());
				alert.initModality(Modality.WINDOW_MODAL);
				Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
				alertStage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() != ButtonType.OK) {
					return false;
				}
			}

			// Save Dialog
			if (globalSettings.isIgnoreSaveDialog() || openProject.getProjectReference().isSync()) {
				saveProject();
			} else {
				SaveDialog alert = new SaveDialog(getStage());
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent()) {
					globalSettings.setIgnoreSaveDialog(alert.isSelected());
					ButtonType buttonType = result.get();
					if (buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
						// Projekt Speichern
						saveProject();
					} else if (buttonType.getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
						return false;
					}
				}
			}

			// Save Config - Ist unabhängig vom Dialog, da es auch an anderen Stellen schon gespeichert wird
			try {
				if (Profile.currentProfile() != null)
					Profile.currentProfile().save();
			} catch (Exception e) {
				Logger.error(e);
				showErrorMessage(Localization.getString(Strings.ERROR_PROFILE_SAVE));
			}

			// MIDI Shutdown
			// Der schließt MIDI, da er es auch öffnet und verantwortlich ist
			if (profileSettings.isMidiActive() && Midi.getInstance().isOpen()) {
				try {
					Midi.getInstance().clearFeedback();
					Midi.getInstance().close();
				} catch (CloseException e) {
					Logger.error(e);
				}
			}

			if (autosaveThread != null && autosaveThread.isAlive()) {
				Logger.debug("Stopping autosave thread");
				autosaveThread.interrupt();
			}
		}
		Platform.exit();
		return true;
	}

	private void saveProject() {
		try {
			if (openProject.getProjectReference() != null) {
				ProjectReferenceManager.saveSingleProject(openProject);
				Logger.info("Saved Project: " + openProject);
			}
		} catch (Exception e) {
			Logger.error(e);
			showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_SAVE));
		}
	}

	@Override
	public void closeProject() {
		// Remove old listener
		if (this.openProject != null) {
			this.openProject.getProjectReference().nameProperty().removeListener(projectTitleListener);
			this.openProject.getPages().removeListener(pagesListener);
			this.openProject.notFoundMediaProperty().removeListener(notFoundListener);
			this.openProject.close();
		}
		removePadContentsFromView();
		this.openProject = null;
	}

	@Override
	public void openProject(Project project) {
		if (this.openProject != null) {
			closeProject();
		}

		openProject = project;

		// Add new Listener
		openProject.getProjectReference().nameProperty().addListener(projectTitleListener);
		openProject.getPages().addListener(pagesListener);
		openProject.notFoundMediaProperty().addListener(notFoundListener);

		volumeChangeListener.setOpenProject(openProject);

		Profile profile = Profile.currentProfile();
		profile.getMappings().getActiveMapping().ifPresent(mapping -> {
			Mapping.setCurrentMapping(mapping);
			Midi.getInstance().showFeedback();
		});

		menuToolbarViewController.setOpenProject(openProject);

		createPadViews();
		showPage(FIRST_PAGE);
		loadUserCss();
		updateWindowTitle();

		notFoundListener.changed(project.notFoundMediaProperty(), 0, project.getNotFoundMedia());
	}

	/*
	 * Page Handling
	 */
	@Override
	public int getPage() {
		return currentPageShowing;
	}

	@Override
	public boolean showPage(Page page) {
		return showPage(page.getPosition());
	}

	@Override
	public boolean showPage(int page) {
		if (openProject == null) {
			return false;
		}

		if (page < 0 || page >= openProject.getPages().size()) {
			return false;
		}

		// Clean
		removePadContentsFromView();
		this.currentPageShowing = page;
		addPadContentsToView();

		if (menuToolbarViewController != null) {
			menuToolbarViewController.highlightPageButton(page);
		}
		loadUserCss();

		PlayPadPlugin.getInstance().getMainViewListeners().forEach(listener -> listener.onCurrentPageChanged(page));

		return true;
	}

	/**
	 * Zeigt die aktuellen Pads von einem Profil zu einer Seite in den entsprechenden Views an.
	 */
	private void addPadContentsToView() {
		ProjectSettings settings = openProject.getSettings();

		for (int i = 0; i < settings.getRows() * settings.getColumns(); i++) {
			if (padViews.size() > i) {
				IPadView view = padViews.get(i);
				Pad pad = openProject.getPad(new PadIndex(i, currentPageShowing));

				view.getViewController().setupPad(pad);
			}
		}
	}

	/**
	 * Entfernt alle Pads auf den Views.
	 */
	private void removePadContentsFromView() {
		// Clean old pads
		for (IPadView padView : padViews) {
			padView.getViewController().removePad();
		}
	}

	/*
	 * PadViews
	 */
	@Override
	@SuppressWarnings("Duplicates")
	public void createPadViews() {
		if (openProject == null) {
			return;
		}
		ProjectSettings projectSettings = openProject.getSettings();

		// Table
		padGridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / projectSettings.getColumns();
		for (int i = 0; i < projectSettings.getColumns(); i++) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			padGridPane.getColumnConstraints().add(c);
		}

		padGridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / projectSettings.getRows();
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
				IPadView padView = mainLayout.createPadView();
				padGridPane.add(padView.getRootNode(), x, y);
				padViews.add(padView);
			}
		}

		menuToolbarViewController.initPageButtons();
		setMinSize();
	}

	@Override
	public List<IPadView> getPadViews() {
		return padViews;
	}

	private void removePadViews() {
		padViews.forEach(view ->
		{
			padGridPane.getChildren().remove(view.getRootNode());
			mainLayout.recyclePadView(view);
		});
		padViews.clear();
	}

	@Override
	public void loadUserCss() {
		if (getStage() == null) {
			return;
		}

		Scene scene = getStage().getScene();

		// Clear Old
		scene.getStylesheets().clear();

		// Layout Spezifisches CSS (Base)
		if (mainLayout.getStylesheet() != null) {
			scene.getStylesheets().add(mainLayout.getStylesheet());
		}

		// design specific css
		if (openProject != null) {
			ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
			PlayPadMain.getProgramInstance().getModernDesign().global().applyStyleSheetToMainViewController(design, this, getStage(), openProject);

			// Adjust colors
			if (Mapping.getCurrentMapping() != null) {
				ColorAdjuster.applyColorsToKeys();
				Midi.getInstance().showFeedback();
			}
		}
	}

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
			Logger.error(e);
		}
	}

	@Override
	public void setMainLayout(MainLayoutFactory mainLayoutConnect) {
		removePadContentsFromView();
		removePadViews();

		this.mainLayout = mainLayoutConnect;
		initMainLayout();
		this.menuToolbarViewController.initLayoutMenu();
	}

	/*
	 * Settings Handling
	 */
	@Override
	public void reloadSettings(Profile old, Profile currentProfile) {

		final DoubleProperty volumeFadeValueProperty = menuToolbarViewController.getVolumeSlider().valueProperty();

		if (old != null) {
			// Unbind Volume Slider
			volumeFadeValueProperty.unbindBidirectional(old.getProfileSettings().volumeProperty());
			volumeFadeValueProperty.removeListener(volumeChangeListener);

			// Clear Feedback on Device (LaunchPad Light off)
			Midi.getInstance().clearFeedback();

			// LockedListener
			old.getProfileSettings().lockedProperty().removeListener(lockedListener);
		}

		// Volume
		volumeFadeValueProperty.bindBidirectional(currentProfile.getProfileSettings().volumeProperty());
		volumeFadeValueProperty.addListener(volumeChangeListener);

		final ProfileSettings profileSettings = currentProfile.getProfileSettings();
		// LockedListener
		profileSettings.lockedProperty().addListener(lockedListener);

		// MIDI
		if (profileSettings.isMidiActive() && profileSettings.getMidiDevice() != null) {
			// Load known MIDI Device
			Worker.runLater(() ->
			{
				loadMidiDevice(profileSettings.getMidiDevice());

				Platform.runLater(() ->
				{
					// Handle Mapper
					if (openProject != null) {
						Midi.getInstance().showFeedback();
					}
				});
			});
		}

		try {
			DefaultRegistry<MainLayoutFactory> registry = PlayPadPlugin.getRegistries().getMainLayouts();
			MainLayoutFactory connect = registry.getFactory(currentProfile.getProfileSettings().getMainLayoutType());
			setMainLayout(connect);
		} catch (NoSuchComponentException e) {
			Logger.error(e);
		}

		loadUserCss();
		if (old != null) {
			showPage(currentPageShowing);
		}
	}

	/**
	 * Init MIDI Device by using the Midi Class and show some feedback the user.
	 *
	 * @param name Device Name
	 */
	private void loadMidiDevice(String name) {
		try {
			final MidiDeviceInfo midiDeviceInfo = Midi.getInstance().getMidiDeviceInfo(name);
			if (midiDeviceInfo != null) {
				Midi.getInstance().openDevice(midiDeviceInfo, Midi.Mode.INPUT, Midi.Mode.OUTPUT);
				notificationPane.showAndHide(Localization.getString(Strings.INFO_MIDI_DEVICE_CONNECTED, name), PlayPadMain.NOTIFICATION_DISPLAY_TIME);
			}
		} catch (NullPointerException e) {
			Logger.error(e);
			showError(Localization.getString(Strings.ERROR_MIDI_DEVICE_UNAVAILABLE, name));
		} catch (IllegalArgumentException | MidiUnavailableException e) {
			showError(Localization.getString(Strings.ERROR_MIDI_DEVICE_BUSY, e.getLocalizedMessage()));
			Logger.error(e);
		}
	}

	/*
	 * Utils
	 */
	@Override
	public <T extends Event> void addListenerForPads(EventHandler<? super T> handler, EventType<T> eventType) {
		for (IPadView view : padViews) {
			view.getRootNode().addEventFilter(eventType, handler);
		}
	}

	@Override
	public <T extends Event> void removeListenerForPads(EventHandler<? super T> handler, EventType<T> eventType) {
		for (IPadView view : padViews) {
			view.getRootNode().removeEventFilter(eventType, handler);
		}
	}

	@Override
	public Screen getScreen() {
		return getStageContainer().map(NVCStage::getScreen).orElse(null);
	}

	@Override
	public Stage getStage() {
		return getStageContainer().map(NVCStage::getStage).orElse(null);
	}

	@Override
	public MenuToolbarViewController getMenuToolbarController() {
		return menuToolbarViewController;
	}

	@Override
	public NotificationPane getNotificationPane() {
		return notificationPane;
	}

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

	@Override
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener) {
		getParent().getScene().addEventHandler(eventType, listener);
	}

	@Override
	public void updateWindowTitle() {
		getStageContainer().ifPresent(sc ->
		{
			if (openProject != null && Profile.currentProfile() != null) {
				getStage().setTitle(Localization.getString(Strings.UI_WINDOW_MAIN_TITLE, openProject.getProjectReference().getName(),
						Profile.currentProfile().getRef().getName()));
			} else {
				getStage().setTitle(Localization.getString(Strings.UI_WINDOW_MAIN_TITLE, "-", "-"));
			}
		});
	}

	@Override
	public void performLayoutDependedAction(MainLayoutHandler runnable) {
		runnable.handle(null, menuToolbarViewController);
		layoutActions.add(runnable);
	}

	@Override
	public void loadKeybinding(KeyCollection keys) {
		if (menuToolbarViewController != null) {
			menuToolbarViewController.loadKeybinding(keys);
		}

		// Plugin Hook
		PlayPadPlugin.getInstance().getMainViewListeners().forEach(MainWindowListener::loadMenuKeyBinding);
	}

	@Override
	public void save() {
		try {
			ProjectReferenceManager.saveProjects();
			ProjectReferenceManager.saveSingleProject(openProject);
			ProfileReferenceManager.saveProfiles();
			Profile.currentProfile().save();
			PlayPadPlugin.getInstance().getGlobalSettings().save();

			notify(Localization.getString(Strings.STANDARD_FILE_SAVE), PlayPadMain.NOTIFICATION_DISPLAY_TIME);
		} catch (IOException e) {
			showError(Localization.getString(Strings.ERROR_PROJECT_SAVE));
			Logger.error(e);
		}
	}
}
