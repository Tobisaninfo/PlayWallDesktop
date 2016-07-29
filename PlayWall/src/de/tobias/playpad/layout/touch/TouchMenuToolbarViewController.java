package de.tobias.playpad.layout.touch;

import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

public class TouchMenuToolbarViewController extends MenuToolbarViewController {

	public TouchMenuToolbarViewController() {
		super("", "", null); // TODO Add Parameters to constructor.
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
