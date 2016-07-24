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

	public void addMenuItem(MenuItem item);

	public int getPage();

	@Deprecated
	public Parent getParent();
	
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener);

	@Deprecated
	public Project getProject();

	@Deprecated
	public Slider getVolumeSlider();

	@Deprecated
	public IMainToolbarViewController getToolbarController();

	public void createPadViews();

	public void showPage(int page);

	public void loadUserCss();

	public void applyColorsToMappers();
	
	public void showLiveInfo();
}
