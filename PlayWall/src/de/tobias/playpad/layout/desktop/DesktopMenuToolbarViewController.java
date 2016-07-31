package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.view.main.MenuType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

public class DesktopMenuToolbarViewController extends BasicMenuToolbarViewController {

	public DesktopMenuToolbarViewController() {
		super("header", "de/tobias/playpad/assets/view/main/desktop/", PlayPadMain.getUiResourceBundle());
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
}
