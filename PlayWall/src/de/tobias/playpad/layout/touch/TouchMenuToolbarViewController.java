package de.tobias.playpad.layout.touch;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;

public class TouchMenuToolbarViewController extends BasicMenuToolbarViewController {

	@FXML protected CheckMenuItem fullScreenMenuItem;
	@FXML protected CheckMenuItem alwaysOnTopItem;
	@FXML protected MenuItem closeMenuItem;

	@FXML protected Label liveLabel;

	private IMainViewController mainViewController;

	public TouchMenuToolbarViewController(IMainViewController mainViewController) {
		super("header", "de/tobias/playpad/assets/view/main/touch/", PlayPadMain.getUiResourceBundle(), mainViewController);
		this.mainViewController = mainViewController;

		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());

		showLiveLabel(false);
	}

	@Override
	public void initPageButtons() {
		pageHBox.getChildren().clear();

		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		for (int i = 0; i < settings.getPageCount(); i++) {
			Button button = new Button(Localization.getString(Strings.UI_Window_Main_PageButton, (i + 1)));
			button.setUserData(i);
			button.setFocusTraversable(false);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void setLocked(boolean looked) {

	}

	@Override
	public void setAlwaysOnTopActive(boolean alwaysOnTopActive) {
		alwaysOnTopItem.setSelected(alwaysOnTopActive);
	}

	@Override
	public void setFullScreenActive(boolean fullScreenActive) {
		fullScreenMenuItem.setSelected(fullScreenActive);
	}

	@Override
	public void addToolbarIcon(Image icon) {
		// TODO Implement
	}

	@Override
	public void removeToolbarIcon(Image icon) {
		// TODO Implement
	}

	@Override
	public void addToolbarItem(Node node) {
		iconHbox.getChildren().add(node);
	}

	@Override
	public void removeToolbarItem(Node node) {
		iconHbox.getChildren().remove(node);
	}

	@Override
	public void addMenuItem(MenuItem item, MenuType type) {
	}

	@Override
	public void removeMenuItem(MenuItem item) {
	}

	@Override
	public boolean isAlwaysOnTopActive() {
		return false; // TODO
	}

	@Override
	public boolean isFullscreenActive() {
		return false; // TODO
	}

	@Override
	public void deinit() {
		toolbarHBox.prefWidthProperty().unbind();
		toolbarHBox.prefHeightProperty().unbind();
	}

	@Override
	public void showLiveLabel(boolean show) {
		liveLabel.setVisible(show);
	}

	private int currentPage = 0;

	@Override
	public void hilightPageButton(int index) {
		if (index >= 0) {
			if (pageHBox.getChildren().size() > currentPage) {
				Node removeNode = pageHBox.getChildren().get(currentPage);
				removeNode.getStyleClass().remove(CURRENT_PAGE_BUTTON);
			}

			if (pageHBox.getChildren().size() > index) {
				Node newNode = pageHBox.getChildren().get(index);
				newNode.getStyleClass().add(CURRENT_PAGE_BUTTON);
				currentPage = index;
			}
		}
	}

	@Override
	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	// Event Handler
	@FXML
	void alwaysOnTopItemHandler(ActionEvent event) {
		boolean selected = alwaysOnTopItem.isSelected();

		mainViewController.getStage().setAlwaysOnTop(selected);
		Profile.currentProfile().getProfileSettings().setWindowAlwaysOnTop(selected);
	}

	@FXML
	void fullScreenMenuItemHandler(ActionEvent event) {
		mainViewController.getStage().setFullScreen(fullScreenMenuItem.isSelected());
	}

	@FXML
	void closeMenuItemHandler(ActionEvent event) {
		MainLayoutConnect defaultLayout = PlayPadPlugin.getRegistryCollection().getMainLayouts().getDefault();

		Profile.currentProfile().getProfileSettings().setMainLayoutType(defaultLayout.getType());
		mainViewController.setMainLayout(defaultLayout);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof Button) {
			Button button = (Button) event.getSource();
			int page = (int) button.getUserData();
			mainViewController.showPage(page);
		}
	}

}