package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

public class DesktopMenuToolbarViewController extends BasicMenuToolbarViewController implements EventHandler<ActionEvent> {

	private IMainViewController mainViewController;

	public DesktopMenuToolbarViewController(IMainViewController controller) {
		super("header", "de/tobias/playpad/assets/view/main/desktop/", PlayPadMain.getUiResourceBundle());
		this.mainViewController = controller;
	}

	@Override
	public void initPages() {
		pageHBox.getChildren().clear();

		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		for (int i = 0; i < settings.getPageCount(); i++) {
			Button button = new Button("Seite: " + (i + 1));
			button.setUserData(i);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
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

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof Button) {
			Button button = (Button) event.getSource();
			int page = (int) button.getUserData();
			mainViewController.showPage(page);
		}
	}
}
