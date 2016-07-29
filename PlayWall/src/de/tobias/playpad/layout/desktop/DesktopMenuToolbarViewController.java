package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class DesktopMenuToolbarViewController extends MenuToolbarViewController {

	@FXML private Label volumeUpLabel;
	@FXML private HBox iconHbox;
	@FXML private MenuItem errorMenu;
	@FXML private HBox pageHBox;
	@FXML private MenuItem saveMenuItem;
	@FXML private HBox toolbarHBox;
	@FXML private CheckMenuItem fullScreenMenuItem;
	@FXML private MenuItem settingsMenuItem;
	@FXML private MenuBar menuBar;
	@FXML private ToolBar toolbar;
	@FXML private Menu recentOpenMenu;
	@FXML private Slider volumeSlider;
	@FXML private Menu extensionMenu;
	@FXML private MenuItem profileMenu;
	@FXML private Label volumeDownLabel;
	@FXML private CheckMenuItem dndModeMenuItem;
	@FXML private CheckMenuItem alwaysOnTopItem;

	public DesktopMenuToolbarViewController() {
		super("header", "de/tobias/playpad/assets/view/main/desktop/", PlayPadMain.getUiResourceBundle());

		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());
	}

	@Override
	public void initPages() {

	}

	@Override
	public void setLocked(boolean looked) {

	}

	@Override
	public void addToolbarIcon(Image icon) {

	}

	@Override
	public void removeToolbarIcon(Image icon) {

	}

	@Override
	public void addMenuItem(MenuItem item, MenuType type) {

	}

	@Override
	public void removeMenuItem(MenuItem item) {

	}

	@Override
	public boolean isAlwaysOnTopActive() {
		return false;
	}

	@Override
	public boolean isFullscreenActive() {
		return false;
	}

	@Override
	public void deinit() {

	}

	// EventHandler
	@FXML
	void newDocumentHandler(ActionEvent event) {

	}

	@FXML
	void openDocumentHandler(ActionEvent event) {

	}

	@FXML
	void saveMenuHandler(ActionEvent event) {

	}

	@FXML
	void profileMenuHandler(ActionEvent event) {

	}

	@FXML
	void printMenuHandler(ActionEvent event) {

	}

	@FXML
	void dndModeHandler(ActionEvent event) {

	}

	@FXML
	void errorMenuHandler(ActionEvent event) {

	}

	@FXML
	void pluginMenuItemHandler(ActionEvent event) {

	}

	@FXML
	void settingsHandler(ActionEvent event) {

	}

	@FXML
	void alwaysOnTopItemHandler(ActionEvent event) {

	}

	@FXML
	void fullScreenMenuItemHandler(ActionEvent event) {

	}

	@FXML
	void aboutMenuHandler(ActionEvent event) {

	}

	@FXML
	void visiteWebsiteMenuHandler(ActionEvent event) {

	}

	@FXML
	void sendErrorMenuItem(ActionEvent event) {

	}

}
