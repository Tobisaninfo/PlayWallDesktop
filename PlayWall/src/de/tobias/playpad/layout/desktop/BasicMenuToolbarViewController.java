package de.tobias.playpad.layout.desktop;

import java.util.ResourceBundle;

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
import javafx.scene.layout.HBox;

public abstract class BasicMenuToolbarViewController extends MenuToolbarViewController {

	@FXML protected Label volumeUpLabel;
	@FXML protected HBox iconHbox;
	@FXML protected MenuItem errorMenu;
	@FXML protected HBox pageHBox;
	@FXML protected MenuItem saveMenuItem;
	@FXML protected HBox toolbarHBox;
	@FXML protected CheckMenuItem fullScreenMenuItem;
	@FXML protected MenuItem settingsMenuItem;
	@FXML protected MenuBar menuBar;
	@FXML protected ToolBar toolbar;
	@FXML protected Menu recentOpenMenu;
	@FXML protected Slider volumeSlider;
	@FXML protected Menu extensionMenu;
	@FXML protected MenuItem profileMenu;
	@FXML protected Label volumeDownLabel;
	@FXML protected CheckMenuItem dndModeMenuItem;
	@FXML protected CheckMenuItem alwaysOnTopItem;

	public BasicMenuToolbarViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);

		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());
	}

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
