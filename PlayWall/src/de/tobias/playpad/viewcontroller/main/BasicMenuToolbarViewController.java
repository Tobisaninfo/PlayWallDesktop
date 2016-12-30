package de.tobias.playpad.viewcontroller.main;

import java.util.ResourceBundle;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;

public abstract class BasicMenuToolbarViewController extends MenuToolbarViewController implements EventHandler<ActionEvent> {

	// Menu
	@FXML protected Label volumeUpLabel;
	@FXML protected HBox iconHbox;

	@FXML protected HBox pageHBox;
	@FXML protected HBox toolbarHBox;
	@FXML protected ToolBar toolbar;
	@FXML protected Menu recentOpenMenu;
	@FXML protected Slider volumeSlider;
	@FXML protected Label volumeDownLabel;

	protected Project openProject; // REFERENCE zu MainViewController

	public BasicMenuToolbarViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	@Override
	public void init() {
		volumeDownLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_DOWN));
		volumeUpLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_UP));

		volumeSlider.setOnScroll(ev ->
		{
			volumeSlider.setValue(volumeSlider.getValue() - ev.getDeltaY() * 0.001);
			volumeSlider.setValue(volumeSlider.getValue() + ev.getDeltaX() * 0.001);
		});
	}

	// Utils
	protected void doAction(Runnable run) {
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
		if (!(project.getActivePlayers() > 0 && globalSettings.isLiveMode())) {
			run.run();
		}
	}

	protected void setKeyBindingForMenu(MenuItem menuItem, Key key) {
		if (key != null) {
			if (!key.getKeyCode().isEmpty()) {
				KeyCombination keyCode = KeyCombination.valueOf(key.getKeyCode());
				if (keyCode != null) {
					menuItem.setAccelerator(keyCode);
				}
			}
		}
	}

	@Override
	public void setOpenProject(Project project) {
		this.openProject = project;
	}
}