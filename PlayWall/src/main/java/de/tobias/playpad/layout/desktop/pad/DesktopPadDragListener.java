package de.tobias.playpad.layout.desktop.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.layout.desktop.DesktopEditMode;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutFactory;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.pad.drag.ContentDragOption;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.pad.drag.PlaylistDragOption;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.view.PadDragOptionView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.EventHandler;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DesktopPadDragListener implements EventHandler<DragEvent> {

	private static final String PAD_INDEX_DATATYPE = "de.tobias.playpad.pad_index";
	private static final DataFormat dataFormat = new DataFormat(PAD_INDEX_DATATYPE);

	private final Pad currentPad;
	private final Pane padViewNode; // Node der PadView

	private final DesktopMainLayoutFactory connect;

	private PadDragOptionView padHud;
	private FileDragOptionView fileHud;

	DesktopPadDragListener(Pad currentPad, IPadView view, DesktopMainLayoutFactory connect) {
		this.currentPad = currentPad;
		this.connect = connect;

		this.padViewNode = view.getRootNode();
	}

	public void addListener() {
		this.padViewNode.setOnDragOver(this::dragOver);
		this.padViewNode.setOnDragExited(event -> dragExited());
		this.padViewNode.setOnDragDropped(this::dragDropped);
		this.padViewNode.setOnDragDetected(this::dragDetected);
	}

	void removeListener() {
		this.padViewNode.setOnDragOver(null);
		this.padViewNode.setOnDragExited(null);
		this.padViewNode.setOnDragDropped(null);
		this.padViewNode.setOnDragDetected(null);
	}

	@Override
	public void handle(DragEvent event) {
		if (event.getEventType() == DragEvent.DRAG_OVER) {
			dragOver(event);
		} else if (event.getEventType() == DragEvent.DRAG_EXITED) {
			dragExited();
		} else if (event.getEventType() == DragEvent.DRAG_DROPPED) {
			dragDropped(event);
		}
	}

	private void dragOver(DragEvent event) {
		if (Profile.currentProfile().getProfileSettings().isLocked() || checkLiveMode()) {
			return;
		}

		if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
			handleFileDropOver(event);
		} else if (event.getDragboard().hasContent(dataFormat)) {
			handlePadDragOver(event);
		}
		event.consume();
	}

	@SuppressWarnings("java:S1066")
	private void handleFileDropOver(DragEvent event) {
		final File file = event.getDragboard().getFiles().get(0);
		if (file.isFile()) {

			final List<Path> paths = event.getDragboard().getFiles().stream().map(File::toPath).collect(Collectors.toList());

			// built-in file support
			final PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();
			final List<PadContentFactory> supportedContentTypes = registry.getPadContentConnectsForFiles(paths);

			if (currentPad.getContent() != null && currentPad.getContent().isPadLoaded()) {
				return;
			}

			final List<ContentDragOption> contentDragOptions = new ArrayList<>(supportedContentTypes);

			if (currentPad.getContent() instanceof Playlistable) {
				if (supportedContentTypes.stream().anyMatch(factory -> factory.getType().equals(currentPad.getContent().getType()))) {
					contentDragOptions.add(new PlaylistDragOption());
				}
			}

			if (!contentDragOptions.isEmpty()) {
				if (fileHud == null) {
					fileHud = new FileDragOptionView(padViewNode);
				}
				fileHud.showOptions(contentDragOptions);

				event.acceptTransferModes(TransferMode.LINK);
			}
		}
	}

	private void handlePadDragOver(DragEvent event) {
		PadIndex index = (PadIndex) event.getDragboard().getContent(dataFormat);
		if (!currentPad.getPadIndex().equals(index)) {

			Collection<PadDragMode> connects = PlayPadPlugin.getRegistries().getDragModes().getComponents();

			if (!connects.isEmpty()) {
				if (padHud == null) {
					padHud = new PadDragOptionView(padViewNode);
				}
				padHud.showDropOptions(connects);

				event.acceptTransferModes(TransferMode.MOVE);
			}
		}
	}

	private void dragExited() {
		if (padHud != null) {
			padHud.hide();
		}
		if (fileHud != null) {
			fileHud.hide();
		}
	}

	// Drag Content ist los gelassen am Ziel
	private void dragDropped(DragEvent event) {
		final Project project = PlayPadPlugin.getInstance().getCurrentProject();

		final Dragboard dragboard = event.getDragboard();
		boolean success = false;

		// File Handling
		if (dragboard.hasFiles()) {
			success = handleFileDragDropped(dragboard);
		}

		// Pad DnD
		if (dragboard.hasContent(dataFormat)) {
			success = handlePadDragDropped(project, dragboard);
		}
		// Event Completion
		event.setDropCompleted(success);
		event.consume();
	}

	private boolean handleFileDragDropped(Dragboard dragboard) {
		final ContentDragOption dragOption = fileHud.getSelectedConnect();
		if (dragOption != null) {
			// stop pad if playing
			if (currentPad.getContent() != null && currentPad.getStatus().equals(PadStatus.PLAY)) {
				currentPad.getContent().stop();
				currentPad.stop();
			}

			dragOption.handleDrop(currentPad, dragboard.getFiles());

			if (currentPad.getController() != null) {
				final IPadView padView = currentPad.getController().getView();
				padView.setContentView(currentPad);
				padView.addDefaultElements(currentPad);
			}

			return true;
		}
		return false;
	}

	private boolean handlePadDragDropped(Project project, Dragboard dragboard) {
		boolean success = false;

		Object data = dragboard.getContent(dataFormat);
		if (data instanceof PadIndex) {
			PadIndex srcIndex = (PadIndex) data;
			PadIndex newIndex = currentPad.getPadIndex(); // Lister ist auf Ziel Pad, daher ist der Index von currentPad

			// Drag handle
			PadDragMode mode = padHud.getSelectedPadDragMode();
			success = mode.handle(srcIndex, newIndex, project);
			padHud.hide();

			// Update der Pad Views nach dem DnD
			IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
			mainViewController.showPage(mainViewController.getPage());

			if (project.getProjectReference().isSync()) {
				CommandManager.execute(Commands.PAD_MOVE);
			}
		}
		return success;
	}

	private void dragDetected(MouseEvent event) {
		if (connect.getEditMode() == DesktopEditMode.DRAG) {
			if (checkLiveMode()) {
				return;
			}

			Dragboard dragboard = padViewNode.startDragAndDrop(TransferMode.MOVE);

			// Create Snapshot
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			WritableImage snapshot = padViewNode.snapshot(parameters, null);
			for (int x = 0; x < snapshot.getWidth(); x++) {
				for (int y = 0; y < snapshot.getHeight(); y++) {
					Color oldColor = snapshot.getPixelReader().getColor(x, y).darker().darker();
					Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity() * 0.5);
					snapshot.getPixelWriter().setColor(x, y, newColor);
				}
			}

			dragboard.setDragView(snapshot);

			ClipboardContent content = new ClipboardContent();
			content.put(dataFormat, currentPad.getPadIndex());
			dragboard.setContent(content);

			event.consume();
		}
	}

	// Utils
	private boolean checkLiveMode() {
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (currentPad.getProject() != null) {
			return globalSettings.isLiveMode() && globalSettings.isLiveModeFile() && currentPad.getProject().getActivePlayers() > 0;
		}
		return false;
	}
}
