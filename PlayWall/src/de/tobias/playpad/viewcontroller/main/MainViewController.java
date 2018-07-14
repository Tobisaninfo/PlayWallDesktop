package de.tobias.playpad.viewcontroller.main;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.mapper.listener.KeyboardHandler;
import de.tobias.playpad.action.mapper.listener.MidiHandler;
import de.tobias.playpad.design.modern.ModernDesignSizeHelper;
import de.tobias.playpad.design.modern.ModernGlobalDesign2;
import de.tobias.playpad.layout.desktop.pad.DesktopPadDragListener;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileListener;
import de.tobias.playpad.profile.ProfileSettings;
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
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
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
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.sound.midi.MidiUnavailableException;
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

	// Mapper
	private Midi midi;
	private MidiHandler midiHandler;
	private KeyboardHandler keyboardHandler;

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

	public MainViewController(Consumer<NVC> onFinish) {
		load("de/tobias/playpad/assets/view/main/", "mainView", PlayPadMain.getUiResourceBundle(), e ->
		{
			NVCStage stage = e.applyViewControllerToStage();
			stage.addCloseHook(this::closeRequest);

			// Init with existing stage
			initMapper(openProject);
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
		notificationPane.getStyleClass().add(org.controlsfx.control.NotificationPane.STYLE_CLASS_DARK);

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
		setMainLayout(PlayPadPlugin.getRegistryCollection().getMainLayouts().getDefault());

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
			e.printStackTrace();
		}
	}

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

	private void initMapper(Project project) {
		this.midi = Midi.getInstance();
		this.midiHandler = new MidiHandler(midi, this, project);
		this.midi.setListener(midiHandler);
		this.keyboardHandler = new KeyboardHandler(project, this);

		// Request Focus for key listener
		getParent().requestFocus();
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
		if (menuToolbarViewController != null && openProject != null)
			menuToolbarViewController.setNotFoundNumber(openProject.getNotFoundMedia());

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

	private boolean closeRequest() {
		if (Profile.currentProfile() != null) {
			ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

			// Frag den Nutzer ob das Programm wirdklich geschlossen werden sol
			// wenn ein Pad noch im Status Play ist
			if (openProject.getActivePlayers() > 0 && globalSettings.isLiveMode()) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
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
		return true;
	}

	private void saveProject() {
		try {
			if (openProject.getProjectReference() != null) {
				ProjectReferenceManager.saveProject(openProject);
				System.out.println("Saved Project: " + openProject);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Project_Save));
		}
	}

	@Override
	public void openProject(Project project) {
		// Remove old listener
		if (this.openProject != null) {
			this.openProject.getProjectReference().nameProperty().removeListener(projectTitleListener);
			this.openProject.getPages().removeListener(pagesListener);
			this.openProject.notFoundMediaProperty().removeListener(notFoundListener);
			this.openProject.close();
		}

		removePadContentsFromView();

		openProject = project;

		// Add new Listener
		openProject.getProjectReference().nameProperty().addListener(projectTitleListener);
		openProject.getPages().addListener(pagesListener);
		openProject.notFoundMediaProperty().addListener(notFoundListener);

		volumeChangeListener.setOpenProject(openProject);
		midiHandler.setProject(project);
		keyboardHandler.setProject(project);
		Profile.currentProfile().getMappings().getActiveMapping().showFeedback(openProject);

		midiHandler.setProject(project);
		keyboardHandler.setProject(project);
		DesktopPadDragListener.setProject(project);

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
			ModernGlobalDesign2 design = Profile.currentProfile().getProfileSettings().getDesign();
			PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyCssMainView(design, this, getStage(), openProject);

			// Mapping feedback
			Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();
			activeMapping.clearFeedback();
			activeMapping.prepareFeedback(openProject);
			activeMapping.adjustPadColorToMapper();
			activeMapping.showFeedback(openProject);
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

	@Override
	public void setMainLayout(MainLayoutFactory mainLayoutConnect) {
		removePadContentsFromView();
		removePadViews();

		this.mainLayout = mainLayoutConnect;
		initMainLayout();
	}

	/*
	 * Settings Handling
	 */
	@Override
	public void reloadSettings(Profile old, Profile currentProfile) {

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

				Platform.runLater(() ->
				{
					// Handle Mapper
					if (currentProfile != null) {
						activeMapping.initFeedbackType();
						if (openProject != null) {
							activeMapping.showFeedback(openProject);
							currentProfile.getMappings().getActiveMapping().adjustPadColorToMapper();
						}
					}
				});
			});
		}

		try {
			DefaultRegistry<MainLayoutFactory> registry = PlayPadPlugin.getRegistryCollection().getMainLayouts();
			MainLayoutFactory connect = registry.getFactory(currentProfile.getProfileSettings().getMainLayoutType());
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

	/**
	 * Init MIDI Device by using the Midi Class and show some feedback the user.
	 *
	 * @param name Device Name
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
		if (getStageContainer().isPresent())
			return getStageContainer().get().getScreen();
		return null;
	}

	@Override
	public Stage getStage() {
		if (getStageContainer().isPresent())
			return getStageContainer().get().getStage();
		return null;
	}

	@Override
	public MenuToolbarViewController getMenuToolbarController() {
		return menuToolbarViewController;
	}

	@Override
	public MidiListener getMidiHandler() {
		return midiHandler;
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
				getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title, openProject.getProjectReference().getName(),
						Profile.currentProfile().getRef().getName()));
			} else {
				getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title));
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
	}

}
