package de.tobias.playpad.viewcontroller.option.project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

public class PathsTabViewController extends ProjectSettingsTabViewController implements IProjectReloadTask {

	// Media Path
	@FXML private TextField mediaPathTextField;
	@FXML private Button mediaPathChooseButton;
	@FXML private CheckBox useMediaPath;

	private transient boolean changedMediaPath = false;
	private transient Optional<Path> oldMediaPath = Optional.empty();

	public PathsTabViewController() {
		super("pathTab.fxml", "de/tobias/playpad/assets/view/option/project/", PlayPadMain.getUiResourceBundle());
	}

	@FXML
	void mediaPathChooseHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getStage());
		if (folder != null) {
			Path path = folder.toPath();
			mediaPathTextField.setText(path.toString());
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
		Path newPath = Paths.get(mediaPathTextField.getText());
		if (!settings.getMediaPath().equals(newPath)) {
			changedMediaPath = true;
			if (settings.getMediaPath() != null && !settings.getMediaPath().toString().isEmpty()) {
				oldMediaPath = Optional.of(settings.getMediaPath());
			}
		}

		if (useMediaPath.isSelected()) {
			settings.setMediaPath(newPath);
		}
		settings.setUseMediaPath(useMediaPath.isSelected());

	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return "Pfade (i18n)"; // TODO
	}

	// Reload Data

	@Override
	public boolean needReload() {
		return changedMediaPath;
	}

	@Override
	public Task<Void> getTask(ProjectSettings settings, Project project, IMainViewController controller) {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(name());
				Path newMediaPath = settings.getMediaPath();

				project.closeFile();

				int i = 0;
				Stream<Path> files = Files.list(oldMediaPath.get());
				files.forEach(file ->
				{
					// BUG Copy not work as expected
					try {
						Files.copy(file, newMediaPath.resolve(file.getFileName()));
						Thread.sleep(500);
					} catch (Exception e) {
						e.printStackTrace();
					}
					updateProgress(i, files.count());
				});
				files.close();

				project.loadPadsContent();
				return null;
			}
		};
	}
}
