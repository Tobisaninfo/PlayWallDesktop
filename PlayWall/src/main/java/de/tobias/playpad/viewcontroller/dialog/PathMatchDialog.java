package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.pad.DesktopPadViewController;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.pad.mediapath.MediaPool;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.cell.path.PathMatchActionCell;
import de.tobias.playpad.viewcontroller.cell.path.PathMatchPathCell;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by tobias on 24.03.17.
 */
public class PathMatchDialog extends NVC {

	public class TempMediaPath {
		private MediaPath mediaPath;
		private ObjectProperty<Path> localPath;
		private BooleanProperty selected;

		private MediaPlayer previewPlayer;

		TempMediaPath(MediaPath mediaPath) {
			this.mediaPath = mediaPath;
			this.localPath = new SimpleObjectProperty<>();
			this.selected = new SimpleBooleanProperty(false);

			this.selected.addListener((observable, oldValue, newValue) -> {
				if (!isMatched() && newValue) {
					this.selected.set(false);
				}
			});
		}

		public MediaPath getMediaPath() {
			return mediaPath;
		}

		public Path getLocalPath() {
			return localPath.get();
		}

		void setLocalPath(Path localPath) {
			this.localPath.set(localPath);
			setStatusLabel();
		}

		public ReadOnlyObjectProperty<Path> localPathProperty() {
			return localPath;
		}

		boolean isSelected() {
			return selected.get();
		}

		public void setSelected(boolean selected) {
			this.selected.set(selected);
		}

		public BooleanProperty selectedProperty() {
			return selected;
		}

		public MediaPlayer getPreviewPlayer() {
			return previewPlayer;
		}

		public void setPreviewPlayer(MediaPlayer previewPlayer) {
			this.previewPlayer = previewPlayer;
		}

		@Override
		public String toString() {
			return "TempMediaPath{" +
					"mediaPath=" + mediaPath +
					", localPath=" + localPath +
					'}';
		}

		public boolean isMatched() {
			return localPath.isNotNull().get();
		}
	}

	@FXML
	private TableView<TempMediaPath> table;
	@FXML
	private TableColumn<TempMediaPath, Boolean> selectColumn;
	@FXML
	private TableColumn<TempMediaPath, String> filenameColumn;
	@FXML
	private TableColumn<TempMediaPath, TempMediaPath> localPathColumn;
	@FXML
	private TableColumn<TempMediaPath, TempMediaPath> actionColumn;

	@FXML
	private Label statusLabel;

	@FXML
	private Button cancelButton;
	@FXML
	private Button finishButton;

	private Project project;
	private List<TempMediaPath> missingMediaPaths;

	// TODO Localize fxml
	public PathMatchDialog(Project project, Window owner) {
		load("view/dialog", "NotFoundDialog", Localization.getBundle());

		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		addCloseKeyShortcut(() -> cancelButton.fire());

		this.project = project;

		missingMediaPaths = project.getPads(p -> p.getStatus() == PadStatus.NOT_FOUND)
				.parallelStream()
				.flatMap(pad -> pad.getPaths().stream())
				.map(TempMediaPath::new)
				.collect(Collectors.toList());

		find(false);

		table.getItems().setAll(missingMediaPaths);
		setStatusLabel();
	}

	@Override
	public void init() {
		table.setRowFactory(param -> {
			TableRow<TempMediaPath> row = new TableRow<>();
			row.setOnMouseClicked(e -> {
				if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
					TempMediaPath item = row.getItem();
					if (item != null) {
						showFileChooser(item);
					}
				}
			});
			return row;
		});

		table.setPlaceholder(new Label());

		selectColumn.setCellFactory(param -> new CheckBoxTableCell<>());
		localPathColumn.setCellFactory(param -> new PathMatchPathCell());
		actionColumn.setCellFactory(param -> new PathMatchActionCell(this));

		selectColumn.setCellValueFactory(param -> param.getValue().selectedProperty());
		filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMediaPath().getFileName()));
		localPathColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
		actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(900);
		stage.setMinHeight(400);
		stage.setMaxHeight(600);
		stage.setTitle(Localization.getString(Strings.UI_DIALOG_NOT_FOUND_TITLE));

		PlayPadPlugin.styleable().applyStyle(stage);
	}

	public void showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
	}

	@FXML
	void cancelHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	void finishHandler(ActionEvent event) {
		missingMediaPaths.stream()
				.filter(TempMediaPath::isSelected)
				.forEach(p -> p.getMediaPath().setPath(p.getLocalPath(), true));
		getStageContainer().ifPresent(NVCStage::close);
	}

	private int getUnmatchedTracksCount() {
		return (int) missingMediaPaths.stream().filter(p -> !p.isMatched()).count();
	}

	private void setStatusLabel() {
		Platform.runLater(() -> statusLabel.setText(Localization.getString(Strings.UI_DIALOG_PATH_MATCH_STATUS, getUnmatchedTracksCount())));
	}

	public void showFileChooser(TempMediaPath item) {
		FileChooser chooser = new FileChooser();
		PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();

		// File Extension
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(Localization.getString(Strings.FILE_FILTER_MEDIA),
				registry.getSupportedFileTypes());
		chooser.getExtensionFilters().add(extensionFilter);

		// Last Folder
		Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(DesktopPadViewController.OPEN_FOLDER);
		if (openFolder != null) {
			File folder = new File(openFolder.toString());
			chooser.setInitialDirectory(folder);
		}

		File file = chooser.showOpenDialog(getContainingWindow());
		if (file != null) {
			Path path = file.toPath();
			item.setLocalPath(path);
			item.setSelected(true);

			// Search for new local paths
			find(true);
		}
	}

	private Set<Path> searchHistory = new HashSet<>();

	private void find(boolean subdirectories) {
		// Check Project
		Worker.runLater(() -> {
			if (!missingMediaPaths.isEmpty()) {
				Set<Path> searchFolders = calculateSearchPaths();

				searchFolders.stream()
						.filter(folder -> !searchHistory.contains(folder))
						.forEach(folder -> {
							searchHistory.add(folder);
							Logger.info("Search in: " + folder);
							this.missingMediaPaths.parallelStream()
									.filter(entry -> !entry.isMatched())
									.forEach(entry -> {
										try {
											Path result = MediaPool.find(entry.getMediaPath().getFileName(), folder, subdirectories);
											if (result != null) {
												Platform.runLater(() -> {
													entry.setLocalPath(result);
													entry.setSelected(true);
												});
											}
										} catch (IOException e) {
											Logger.error(e);
										}
									});
						});
				Platform.runLater(() -> table.getItems().setAll(missingMediaPaths));
			}
		});
	}

	private Set<Path> calculateSearchPaths() {
		Set<Path> searchFolders = project.getPads(p -> p.getStatus() == PadStatus.READY)
				.stream()
				.flatMap(pad -> pad.getPaths().stream())

				.filter(mediaPath -> Files.exists(mediaPath.getPath()))
				.map(mediaPath -> mediaPath.getPath().getParent())

				.collect(Collectors.toSet());

		missingMediaPaths.stream()
				.filter(TempMediaPath::isMatched)
				.filter(mediaPath -> Files.exists(mediaPath.getLocalPath()))
				.map(mediaPath -> mediaPath.getLocalPath().getParent())
				.forEach(searchFolders::add);

		return searchFolders;
	}
}
