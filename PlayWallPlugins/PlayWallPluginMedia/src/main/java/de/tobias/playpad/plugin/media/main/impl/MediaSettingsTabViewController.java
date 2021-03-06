package de.tobias.playpad.plugin.media.main.impl;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.plugin.media.main.VideoSettings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

import java.util.ArrayList;
import java.util.List;

public class MediaSettingsTabViewController extends ProfileSettingsTabViewController implements IProfileReloadTask {

	@FXML
	private Pane screenViewPane;
	@FXML
	private ComboBox<Integer> screenComboBox;
	@FXML
	private CheckBox fullscreenCheckBox;
	@FXML
	private CheckBox videoOpenAtLaunchCheckBox;

	private VideoSettings settings;

	private boolean changeSettings = false;

	public MediaSettingsTabViewController(VideoSettings settings) {
		load("view", "SettingsPane", Localization.getBundle());
		this.settings = settings;
	}

	@Override
	public void init() {
		// Video
		createScreenView();
	}

	private void createScreenView() {
		List<Screen> screens = Screen.getScreens();
		List<StackPane> rects = new ArrayList<>();

		screenComboBox.getItems().clear();

		double minX = 0;
		double minY = 0;
		int index = 1;

		for (Screen screen : screens) {
			screenComboBox.getItems().add(index);

			StackPane stackPane = new StackPane();
			stackPane.relocate(screen.getBounds().getMinX() * 0.1, screen.getBounds().getMinY() * 0.1);
			stackPane.resize(screen.getBounds().getWidth() * 0.1, screen.getBounds().getHeight() * 0.1);

			Rectangle rec = new Rectangle(screen.getBounds().getWidth() * 0.1, screen.getBounds().getHeight() * 0.1);
			rec.setStrokeWidth(1.0);
			rec.setStroke(Color.BLACK);
			rec.setFill(Color.TRANSPARENT);

			stackPane.getChildren().addAll(rec, new Label(String.valueOf(index++)));

			if (minX > screen.getBounds().getMinX() * 0.1)
				minX = screen.getBounds().getMinX() * 0.1;
			if (minY > screen.getBounds().getMinY() * 0.1)
				minY = screen.getBounds().getMinY() * 0.1;
			rects.add(stackPane);
			screenViewPane.getChildren().add(stackPane);

		}

		double width = 0;
		double height = 0;
		for (StackPane stackPane : rects) {
			stackPane.relocate(stackPane.getLayoutX() - minX, stackPane.getLayoutY() - minY);
			width += stackPane.getWidth();
			if (height < stackPane.getHeight() + stackPane.getLayoutX())
				height = stackPane.getHeight() + stackPane.getLayoutX();
		}

		screenViewPane.setMaxWidth(width);
		screenViewPane.setMaxHeight(height + 20);
	}

	@Override
	public void loadSettings(Profile profile) {
		screenComboBox.setValue(settings.getScreenId() + 1);
		fullscreenCheckBox.setSelected(settings.isFullScreen());
		videoOpenAtLaunchCheckBox.setSelected(settings.isOpenAtLaunch());
	}

	@Override
	public void saveSettings(Profile profile) {
		if (settings.getScreenId() != (screenComboBox.getValue() != null ? screenComboBox.getValue() - 1 : 0)) {
			changeSettings = true;
		}
		if (settings.isFullScreen() != fullscreenCheckBox.isSelected()) {
			changeSettings = true;
		}
		if (settings.isOpenAtLaunch() != videoOpenAtLaunchCheckBox.isSelected()) {
			changeSettings = true;
		}
		settings.setScreenId(screenComboBox.getValue() != null ? screenComboBox.getValue() - 1 : 0);
		settings.setFullScreen(fullscreenCheckBox.isSelected());
		settings.setOpenAtLaunch(videoOpenAtLaunchCheckBox.isSelected());
	}

	@Override
	public boolean needReload() {
		return changeSettings;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public Runnable getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return MediaPluginImpl.getInstance().getVideoViewController()::reloadSettings;
	}

	@Override
	public String name() {
		return Localization.getString("plugin.media.settings.tab");
	}
}
