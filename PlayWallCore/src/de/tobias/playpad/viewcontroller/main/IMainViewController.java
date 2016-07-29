package de.tobias.playpad.viewcontroller.main;

import de.tobias.playpad.project.Project;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public interface IMainViewController {

	public void setGridColor(Color color);

	public Stage getStage();

	public default void addMenuItem(MenuItem item) {}

	public int getPage();

	public Parent getParent();

	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener);

	@Deprecated
	public default Project getProject() {
		return null;
	}

	@Deprecated
	public default Slider getVolumeSlider() {
		return null;
	}

	@Deprecated
	public default IMainToolbarViewController getToolbarController() {
		return null;
	}

	public void createPadViews();

	public void showPage(int page);

	public void loadUserCss();

	public void applyColorsToMappers();

	public default void showLiveInfo() {}
}
