package de.tobias.playpad.viewcontroller.main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.MidiUnavailableException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.action.feedback.ColorAssociator;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperFeedbackable;
import de.tobias.playpad.action.mapper.listener.KeyboardHandler;
import de.tobias.playpad.action.mapper.listener.MidiHandler;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.PadView;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.dialog.ErrorSummaryDialog;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import de.tobias.playpad.viewcontroller.pad.PadViewController;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

// TODO Extract Color Adjust methodes
public class MainViewController extends ViewController implements IMainViewController, NotificationHandler, ProfileListener {

	private static final String CURRENT_PAGE_BUTTON = "current-page-button";

	// UI
	@FXML protected MainMenuBarController menuBarController;
	@FXML protected MainToolbarController toolbarController;
	@FXML private GridPane padGridPane;

	@FXML private Label liveLabel;

	@FXML private AnchorPane gridContainer;
	private NotificationPane notificationPane;

	private ErrorSummaryDialog errorSummaryDialog;

	// Model
	private Project project;
	protected List<IPadViewController> padViewList = new ArrayList<>();

	// Current View Items
	private int pageNumber;

	// Mapper
	private Midi midi;
	private MidiHandler midiHandler;
	private KeyboardHandler keyboardHandler;

	// Style
	private Color gridColor;

	public MainViewController(Project project, List<WindowListener<IMainViewController>> listener) {
		super("mainView", "de/tobias/playpad/assets/view/main/", null, PlayPadMain.getUiResourceBundle());

		// Include FXML Setup
		toolbarController.setMainViewController(this);
		menuBarController.setMainViewController(this);

		padGridPane.setGridLinesVisible(true);

		// Settings Setup
		Profile.registerListener(this);

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

		/*
		 * Mapper Setup & Listener
		 */
		this.midi = Midi.getInstance();
		this.midiHandler = new MidiHandler(midi, this, project);
		this.midi.setListener(midiHandler);
		this.keyboardHandler = new KeyboardHandler(project, this);

		// Setup
		errorSummaryDialog = new ErrorSummaryDialog(getStage());
		getStage().toFront();

		// setup project
		setProject(project);

		// Setup Settings
		reloadSettings(null, Profile.currentProfile());

		// Listener
		listener.forEach(l -> l.onInit(this));
	}

	@Override
	public void init() {
		padGridPane.getStyleClass().add("pad-grid");

		menuBarController.getExtensionMenu().setVisible(false);

		liveLabel.setVisible(false);
		liveLabel.getStyleClass().add("live-label");

		notificationPane = new NotificationPane(padGridPane);
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

		gridContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);

		getStage().fullScreenProperty().addListener((a, b, c) ->
		{
			menuBarController.getFullScreenMenuItem().setSelected(c);
		});

		// Lautstärke Veränderung
		toolbarController.getVolumeSlider().valueProperty().addListener((a, b, c) ->
		{
			setPadVolume(c.doubleValue());
		});
	}

	public void setPadVolume(double volume) {
		for (Pad pad : project.getPads().values()) {
			if (pad != null)
				pad.setMasterVolume(volume);
		}
	}

	public void setTitle() {
		if (project != null && Profile.currentProfile() != null) {
			getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title, project.getRef().getName(),
					Profile.currentProfile().getRef().getName()));
		} else {
			getStage().setTitle(Localization.getString(Strings.UI_Window_Main_Title));
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

	@Override
	protected void loadSettings(BasicControllerSettings settings) {
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
		settings.addUserInfo("x", getStage().getX());
		settings.addUserInfo("y", getStage().getY());
		settings.width = getStage().getWidth();
		settings.height = getStage().getHeight();
	}

	public void setProject(Project project) {
		if (this.project != null) {
			for (IPadViewController controller : padViewList) {
				controller.unconnectPad();
			}
			// Speichert das alte Projekt, bevor ein neues geladen wird
			try {
				this.project.save();
			} catch (IOException e) {
				e.printStackTrace();
				showError(Localization.getString(Strings.Error_Project_Save, e.getLocalizedMessage()));
			}
		}
		this.project = project;

		midiHandler.setProject(project);
		keyboardHandler.setProject(project);
		PadDragListener.setProject(project);

		errorSummaryDialog.setProject(project);
		menuBarController.createRecentDocumentMenuItems();
		setTitle();
	}

	// GUI Helping Methoden
	public void loadUserCss() {
		Profile.currentProfile().currentLayout().applyCssMainView(this, getStage(), project);
		applyColorsToMappers();
	}

	public void applyColorsToMappers() {
		// Apply Layout to Mapper
		List<CartAction> actions = Profile.currentProfile().getMappings().getActiveMapping().getActions(CartActionConnect.TYPE);
		for (CartAction cartAction : actions) {
			if (cartAction.isAutoFeedbackColors()) {
				for (Mapper mapper : cartAction.getMappers()) {
					if (mapper instanceof MapperFeedbackable) {
						mapColorForMapper(cartAction, mapper);
					}
				}
			}
		}
	}

	private void mapColorForMapper(CartAction cartAction, Mapper mapper) {
		MapperFeedbackable feedbackable = (MapperFeedbackable) mapper;
		if (feedbackable.supportFeedback() && mapper instanceof ColorAssociator) {
			ColorAssociator colorAssociator = (ColorAssociator) mapper;

			Pad pad = project.getPad(cartAction.getCart());
			Color layoutStdColor = null;
			Color layoutEvColor = null;

			if (pad.isCustomLayout()) {
				CartDesign layout = pad.getLayout();
				if (layout instanceof DesignColorAssociator) {
					DesignColorAssociator associator = (DesignColorAssociator) layout;
					layoutStdColor = associator.getAssociatedStandardColor();
					layoutEvColor = associator.getAssociatedEventColor();
				}
			} else {
				GlobalDesign layout = Profile.currentProfile().currentLayout();
				if (layout instanceof DesignColorAssociator) {
					DesignColorAssociator associator = (DesignColorAssociator) layout;
					layoutStdColor = associator.getAssociatedStandardColor();
					layoutEvColor = associator.getAssociatedEventColor();
				}
			}

			if (layoutStdColor != null) {
				DisplayableFeedbackColor associator = Mapper.searchColor(colorAssociator, FeedbackMessage.STANDARD, layoutStdColor);
				colorAssociator.setColor(FeedbackMessage.STANDARD, associator.midiVelocity());
			}

			if (layoutEvColor != null) {
				DisplayableFeedbackColor associator = Mapper.searchColor(colorAssociator, FeedbackMessage.EVENT, layoutEvColor);
				colorAssociator.setColor(FeedbackMessage.EVENT, associator.midiVelocity());
			}
		}
	}

	/**
	 * Erstellt Constraints von GridView, Erstellt PadViews, Lädt CSS, Set Min Size vom Fendster
	 */
	public void createPadViews() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// Table
		padGridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / (double) profileSettings.getColumns();
		for (int i = 0; i < profileSettings.getColumns(); i++) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			padGridPane.getColumnConstraints().add(c);
		}

		padGridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / (double) profileSettings.getRows();
		for (int i = 0; i < profileSettings.getRows(); i++) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight(yPercentage * 100);
			padGridPane.getRowConstraints().add(c);
		}

		// Pads - Remove Old PadViews
		padGridPane.getChildren().removeIf(t ->
		{
			if (t instanceof PadView) {
				((PadView) t).getController().unconnectPad();
				return true;
			} else {
				return false;
			}
		});
		padViewList.clear();

		// Neue PadViews
		for (int y = 0; y < profileSettings.getRows(); y++) {
			for (int x = 0; x < profileSettings.getColumns(); x++) {
				IPadViewController controller = new PadViewController();
				IPadView node = controller.getParent();
				if (node instanceof PadView) {
					padGridPane.add((Node) node, x, y);
					padViewList.add(controller);
				}
			}
		}

		// Min Size of window
		GlobalDesign currentLayout = Profile.currentProfile().currentLayout();
		double minWidth = currentLayout.getMinWidth(profileSettings.getColumns());
		double minHeight = currentLayout.getMinHeight(profileSettings.getRows());

		getStage().setMinWidth(minWidth);
		if (OS.getType() == OSType.MacOSX) {
			getStage().setMinHeight(minHeight + 100);
		} else {
			getStage().setMinHeight(minHeight + 150);
		}
	}

	/**
	 * Setzt die Pads in die Views und Cleared die alten Views. Lädt für die neuen Pads das Layout neu.
	 * 
	 * @param newPage
	 */
	public synchronized void showPage(int newPage) {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		if (!(newPage >= 0 && newPage < profileSettings.getPageCount())) {
			return;
		}

		ProfileSettings settings = Profile.currentProfile().getProfileSettings();
		if (settings.isLiveMode() && settings.isLiveModePage() && getProject().getPlayedPlayers() > 0) {
			showLiveInfo();
			return;
		}

		// Button in Toolbar
		Button oldButton = (Button) toolbarController.getPageHBox().getChildren().get(pageNumber); // Der Aktuell
																									// andersfarbende
																									// Button
		oldButton.getStyleClass().remove(CURRENT_PAGE_BUTTON);

		this.pageNumber = newPage;

		// alte Pads weg
		padViewList.forEach(i -> i.unconnectPad());

		// Neue Pads anzeigen
		int index = pageNumber * profileSettings.getRows() * profileSettings.getColumns();
		for (int i = 0; i < profileSettings.getRows() * profileSettings.getColumns(); i++) {
			if (padViewList.size() > i) {
				IPadViewController view = padViewList.get(i);
				view.setPad(project.getPad(index));
			}
			index++;
		}

		// Button in Toolbar anders färben
		Button newButton = (Button) toolbarController.getPageHBox().getChildren().get(pageNumber);
		newButton.getStyleClass().add(CURRENT_PAGE_BUTTON);

		// Handle Mapper
		if (Profile.currentProfile() != null) {
			Profile.currentProfile().getMappings().getActiveMapping().showFeedback(project, this);
		}

		// GUI Styling
		loadUserCss();
	}

	public Slider getVolumeSlider() {
		return toolbarController.getVolumeSlider();
	}

	// TODO REDO
	@Override
	public boolean closeRequest() {
		if (errorSummaryDialog != null)
			errorSummaryDialog.getStage().close();

		if (Profile.currentProfile() != null) {
			ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();

			// Frag den Nutzer ob das Programm wirdklich geschlossen werden sol
			// wenn ein Pad noch im Status Play ist
			if (project.getPlayedPlayers() > 0 && profilSettings.isLiveMode()) {
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
						if (project.getRef() != null) {
							project.save();
							System.out.println("Saved Project: " + project);
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

		if (

		getStage().isIconified()) {
			getStage().setIconified(false);
		}

		// Verbindung von Pad und PadView wird getrennt. Zudem wird bei PLAY
		// oder PAUSE auf STOP gesetzt
		padViewList.forEach(padView -> padView.unconnectPad());

		saveSettings();
		return true;
	}

	/*
	 * MIDI
	 */

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
			showError(Localization.getString(Strings.Error_Midi_Device_Unavailible, name));
		} catch (IllegalArgumentException | MidiUnavailableException e) {
			showError(Localization.getString(Strings.Error_Midi_Device_Busy, e.getLocalizedMessage()));
			e.printStackTrace();
		}
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
	public int getPage() {
		return pageNumber;
	}

	private boolean shown = false;

	public void showLiveInfo() {
		if (!shown) {
			toolbarController.getToolbarHBox().setOpacity(0.5);
			liveLabel.setVisible(true);
			shown = true;
			Worker.runLater(() ->
			{
				try {
					Thread.sleep(PlayPadMain.displayTimeMillis * 2);
				} catch (Exception e) {}
				Platform.runLater(() ->
				{
					toolbarController.getToolbarHBox().setOpacity(1);
					liveLabel.setVisible(false);
					shown = false;
				});
			});
		}
	}

	@Override
	public void reloadSettings(Profile old, Profile currentProfile) {
		final DoubleProperty valueProperty = toolbarController.getVolumeSlider().valueProperty();

		if (old != null) {
			// Unbind Volume Slider
			valueProperty.unbindBidirectional(old.getProfileSettings().volumeProperty());
			// Clear Feedback on Devie (LaunchPad Light off)
			old.getMappings().getActiveMapping().getActions().forEach(action -> action.clearFeedback());
		}

		// Pad iund Page GUI
		createPadViews();
		toolbarController.createPageButtons();

		// Volume
		valueProperty.bindBidirectional(currentProfile.getProfileSettings().volumeProperty());

		final ProfileSettings profilSettings = currentProfile.getProfileSettings();
		final Mapping activeMapping = currentProfile.getMappings().getActiveMapping();

		// MIDI
		if (profilSettings.isMidiActive() && profilSettings.getMidiDevice() != null) {
			// Load known MIDI Device
			Worker.runLater(() ->
			{
				loadMidiDevice(profilSettings.getMidiDevice());

				applyColorsToMappers();

				Platform.runLater(() ->
				{
					// Handle Mapper
					if (Profile.currentProfile() != null) {
						activeMapping.initFeedback();
						activeMapping.showFeedback(project);
					}
				});
			});
		}

		// WINDOW Settings
		menuBarController.getAlwaysOnTopItem().setSelected(profilSettings.isWindowAlwaysOnTop());
		getStage().setAlwaysOnTop(profilSettings.isWindowAlwaysOnTop());

		setTitle();
		showPage(pageNumber); // Show Mapper Feedback und apply css und zeigt pads
	}

	@Override
	public Project getProject() {
		return project;
	}

	public MidiHandler getMidiHandler() {
		return midiHandler;
	}

	public MainToolbarController getToolbarController() {
		return toolbarController;
	}

	// Plugins
	/**
	 * Fügt ein MenuItem ins Menu hinzu
	 * 
	 * @param item
	 * 
	 * @since 2.0.0
	 */
	public void addMenuItem(MenuItem item) {
		menuBarController.getExtensionMenu().getItems().add(item);
		if (!menuBarController.getExtensionMenu().isVisible()) {
			menuBarController.getExtensionMenu().setVisible(true);
		}
	}

	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
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
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener) {
		getParent().getScene().addEventHandler(eventType, listener);
	}

	@Override
	public List<IPadViewV2> getPadViews() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MenuToolbarViewController getMenuToolbarController() {
		// TODO Auto-generated method stub
		return null;
	}
}
