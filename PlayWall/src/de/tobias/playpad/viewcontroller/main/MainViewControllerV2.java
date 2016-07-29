package de.tobias.playpad.viewcontroller.main;

import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutConnect;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.utils.ui.BasicControllerSettings;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

// TODO Extract Color Adjust methodes
public class MainViewControllerV2 extends ViewController implements IMainViewController, NotificationHandler, ProfileListener {

	private static final String CURRENT_PAGE_BUTTON = "current-page-button";

	@FXML private VBox headerBox;
	@FXML private GridPane padGridPane;

	private MenuToolbarViewController menuToolbarViewController;

	private MainLayoutConnect mainLayout;

	public MainViewControllerV2(List<WindowListener<IMainViewController>> listener) {
		super("mainViewV2", "de/tobias/playpad/assets/view/main/", null, PlayPadMain.getUiResourceBundle());
		setMainLayout(new DesktopMainLayoutConnect());
	}

	// main layout
	public MainLayoutConnect getMainLayout() {
		return mainLayout;
	}

	public void setMainLayout(MainLayoutConnect mainLayoutConnect) {
		this.mainLayout = mainLayoutConnect;
		initMainLayout();
	}

	private void initMainLayout() {
		if (menuToolbarViewController != null) {
			menuToolbarViewController.deinit();
		}

		headerBox.getChildren().clear();
		menuToolbarViewController = mainLayout.createMenuToolbar(this);
		headerBox.getChildren().add(menuToolbarViewController.getParent());
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

	// project
	/**
	 * Öffnet ein Project. Das akutelle project ist in PlayPadImpl gespeichert.
	 * 
	 * @param project
	 *            neues Project
	 */
	public void openProject(Project project) {

	}

	// Pad, Pages
	@Override
	public void createPadViews() {

	}

	@Override
	public void showPage(int page) {

	}

	@Override
	public int getPage() {
		return 0;
	}

	// Settings
	@Override
	public void reloadSettings(Profile oldProfile, Profile currentProfile) {

	}

	@Override
	public void setGridColor(Color color) {

	}

	// Notification
	@Override
	public void notify(String text, long duration) {

	}

	@Override
	public void notify(String text, long duration, Runnable finish) {

	}

	@Override
	public void showError(String message) {

	}

	@Override
	public void hide() {

	}

	// Utils
	@Override
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener) {
		getParent().getScene().addEventHandler(eventType, listener);
	}

	@Override
	public void loadUserCss() {

	}

	@Override
	public void applyColorsToMappers() {

	}
}
