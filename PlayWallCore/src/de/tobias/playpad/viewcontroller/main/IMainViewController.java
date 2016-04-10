package de.tobias.playpad.viewcontroller.main;

import de.tobias.playpad.project.Project;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public interface IMainViewController {

	public void setGridColor(Color color);

	public Stage getStage();

	public void addMenuItem(MenuItem item);

	public int getPage();

	public Parent getParent();

	public Project getProject();

	public Slider getVolumeSlider();

	public IMainToolbarViewController getToolbarController();

	public void createPadViews();

	public void showPage(int page);

	public void loadUserCss();
}
