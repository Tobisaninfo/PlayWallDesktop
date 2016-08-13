package de.tobias.playpad.viewcontroller.option.project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class PathsTabViewController extends ProjectSettingsTabViewController {

	// Media Path
	@FXML private TextField mediaPathTextField;
	@FXML private Button mediaPathChooseButton;
	@FXML private CheckBox useMediaPath;

	public PathsTabViewController() {
		super("pathTab.fxml", "de/tobias/playpad/assets/view/option/project/", PlayPadMain.getUiResourceBundle());
		// TODO Auto-generated constructor stub
	}

	@FXML
	void mediaPathChooseHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getStage());
		if (folder != null) {
			Path path = folder.toPath();
		}
	}

	@Override
	public void loadSettings(ProjectSettings settings) {
		if (settings.isUseMediaPath())
			mediaPathTextField.setText(settings.getMediaPath().toString());
		useMediaPath.setSelected(settings.isUseMediaPath());

	}

	@Override
	public void saveSettings(ProjectSettings settings) {
		if (useMediaPath.isSelected()) {
			settings.setMediaPath(Paths.get(mediaPathTextField.getText()));
		}
		settings.setUseMediaPath(useMediaPath.isSelected());

	}

	@Override
	public boolean needReload() {
		return false;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return "Pfade (i18n)"; // TODO
	}

}
