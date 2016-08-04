package de.tobias.playpad.layout.touch;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;

public class TouchMenuToolbarViewController extends BasicMenuToolbarViewController {

	public TouchMenuToolbarViewController(IMainViewController mainViewController) {
		super("header", "de/tobias/playpad/assets/view/main/touch/", PlayPadMain.getUiResourceBundle(), mainViewController);
	}

	@Override
	public void initPageButtons() {

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
	public void addToolbarItem(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeToolbarItem(Node node) {
		// TODO Auto-generated method stub

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
	public Slider getVolumeSlider() {
		return null;
	}

	@Override
	public void handle(ActionEvent event) {

	}

}