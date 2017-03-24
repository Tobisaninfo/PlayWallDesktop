package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.pad.mediapath.MediaPool;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.cell.NotFoundActionCell;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tobias on 24.03.17.
 */
public class NotFoundDialog extends NVC {

	public class TempMediaPath {
		private MediaPath mediaPath;
		private ObjectProperty<Path> localPath;
		private BooleanProperty selected;

		private MediaPlayer previewPlayer;

		TempMediaPath(MediaPath mediaPath) {
			this.mediaPath = mediaPath;
			this.localPath = new SimpleObjectProperty<>();
			this.selected = new SimpleBooleanProperty(false);
		}

		MediaPath getMediaPath() {
			return mediaPath;
		}

		public Path getLocalPath() {
			return localPath.get();
		}

		public void setLocalPath(Path localPath) {
			this.localPath.set(localPath);
		}

		ObjectProperty<Path> localPathProperty() {
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

		boolean isMatched() {
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
	private TableColumn<TempMediaPath, Path> localPathColumn;
	@FXML
	private TableColumn<TempMediaPath, TempMediaPath> actionColumn;

	@FXML
	private Label statusLabel;

	@FXML
	private Button cancelButton;
	@FXML
	private Button finishButton;

	private Project project;
	private List<TempMediaPath> mediaPaths;

	public NotFoundDialog(Project project, Window owner) {
		load("de/tobias/playpad/assets/dialog/", "notFoundDialog", PlayPadMain.getUiResourceBundle());

		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		addCloseKeyShortcut(() -> cancelButton.fire());

		this.project = project;

		List<MediaPath> missingMediaPaths = new ArrayList<>();
		project.getPads(p -> p.getStatus() == PadStatus.NOT_FOUND)
				.stream()
				.map(Pad::getPaths)
				.forEach(missingMediaPaths::addAll);
		mediaPaths = missingMediaPaths.stream()
				.map(TempMediaPath::new)
				.collect(Collectors.toList());

		find(false);

		table.getItems().setAll(mediaPaths);
	}

	@Override
	public void init() {
		selectColumn.setCellFactory(table -> new CheckBoxTableCell<>());
		actionColumn.setCellFactory(table -> new NotFoundActionCell(this));

		selectColumn.setCellValueFactory(param -> param.getValue().selectedProperty());
		filenameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getMediaPath().getFileName()));
		localPathColumn.setCellValueFactory(param -> param.getValue().localPathProperty());
		actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(700);
		stage.setMinHeight(400);
		stage.setMaxHeight(600);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_NotFound_Title));

		Profile.currentProfile().currentLayout().applyCss(stage);
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
		mediaPaths.stream()
				.filter(TempMediaPath::isSelected)
				.forEach(p -> p.getMediaPath().setPath(p.getLocalPath(), true));
		getStageContainer().ifPresent(NVCStage::close);
	}

	private List<Path> searchHistory = new ArrayList<>();

	public void find(boolean subdirs) {
		// Check Project
		Worker.runLater(() -> {
			if (!mediaPaths.isEmpty()) {
				Set<MediaPath> legalPaths = new HashSet<>();
				project.getPads(p -> p.getStatus() == PadStatus.READY)
						.stream()
						.map(Pad::getPaths)
						.forEach(legalPaths::addAll);
				Collection<Path> folders = legalPaths.stream()
						.filter(mediaPath -> mediaPath.getPath() != null)
						.filter(mediaPath -> Files.exists(mediaPath.getPath()))
						.map(mediaPath -> mediaPath.getPath().getParent())
						.filter(path -> !searchHistory.contains(path))
						.collect(Collectors.toSet());

				for (Path folder : folders) {
					searchHistory.add(folder);
					System.out.println("Search in: " + folder);
					for (TempMediaPath mediaPath : this.mediaPaths) {
						if (!mediaPath.isMatched()) {
							try {
								Path result = MediaPool.find(mediaPath.getMediaPath().getFileName(), folder, false);
								mediaPath.setLocalPath(result);
								if (result != null) {
									mediaPath.setSelected(true);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
	}
}
