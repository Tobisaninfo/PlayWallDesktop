package de.tobias.playpad.viewcontroller.option.project;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.io.FileUtils;
import de.thecodelabs.utils.io.PathUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.path.MultiPathContent;
import de.tobias.playpad.pad.content.path.SinglePathContent;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class PathsTabViewController extends ProjectSettingsTabViewController implements IProjectReloadTask {

	// Media Path
	@FXML
	private TextField mediaPathTextField;
	@FXML
	private Button mediaPathChooseButton;
	@FXML
	private CheckBox useMediaPath;

	private transient boolean changedMediaPath = false;
	private transient Optional<Path> currentMediaPath = Optional.empty();
	private transient Optional<Path> oldMediaPath = Optional.empty();

	PathsTabViewController() {
		load("view/option/project", "PathTab", Localization.getBundle());
	}

	@FXML
	void mediaPathChooseHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getContainingWindow());
		if (folder != null) {
			Path path = folder.toPath();

			if (currentMediaPath.isPresent()) {
				boolean subDirectory = PathUtils.isSubDirectory(currentMediaPath.get(), path);
				if (subDirectory) {
					showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_MEDIA_PATH));
					return;
				}
			}

			mediaPathTextField.setText(path.toString());
		}
	}

	@Override
	public void loadSettings(ProjectSettings settings) {
		if (settings.isUseMediaPath()) {
			mediaPathTextField.setText(settings.getMediaPath().toString());
			currentMediaPath = Optional.of(settings.getMediaPath());
		}
		useMediaPath.setSelected(settings.isUseMediaPath());

	}

	@Override
	public void saveSettings(ProjectSettings settings) {
		Path newPath = Paths.get(mediaPathTextField.getText());
		if (settings.getMediaPath() != null) {
			if (!settings.getMediaPath().equals(newPath)) {
				if (settings.getMediaPath() != null && !settings.getMediaPath().toString().isEmpty()) {
					changedMediaPath = true;
					oldMediaPath = Optional.of(settings.getMediaPath());
				}
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
		return Localization.getString(Strings.UI_WINDOW_SETTINGS_PATHS_TITLE);
	}

	// Reload Data

	@Override
	public boolean needReload() {
		return changedMediaPath;
	}

	@Override
	public Runnable getTask(ProjectSettings settings, Project project, IMainViewController controller) {
		return () -> {
			Path newMediaPath = settings.getMediaPath();

			project.closeFile();

			// TODO Reimplement

			for (Pad pad : project.getPads()) {
				try {
					if (pad.getStatus() != PadStatus.EMPTY) {
						PadContent content = pad.getContent();
						if (content instanceof SinglePathContent) {
							Path path = ((SinglePathContent) content).getPath();
							Path copiedFile = newMediaPath.resolve(path.getFileName());

							Files.copy(path, copiedFile, StandardCopyOption.REPLACE_EXISTING);

							Platform.runLater(() ->
							{
								// content.handlePath(copiedFile); TODO Media Path Handler
							});
						} else if (content instanceof MultiPathContent) {
							MultiPathContent pathHandler = (MultiPathContent) content;
							List<Path> paths = pathHandler.getPaths();
							// TEST handle Paths in PadContent

							pathHandler.clearPaths();

							for (Path path : paths) {
								Path copiedFile = newMediaPath.resolve(path.getFileName());

								Files.copy(path, copiedFile, StandardCopyOption.REPLACE_EXISTING);
								// content.handlePath(copiedFile); TODO Media Path Handler
							}
						}
					}
				} catch (Exception e) {
					Logger.error(e);
				}
			}

			if (oldMediaPath.isPresent())
				try {
					FileUtils.deleteDirectory(oldMediaPath.get());
				} catch (IOException e) {
					Logger.error(e);
				}
		};
	}
}
