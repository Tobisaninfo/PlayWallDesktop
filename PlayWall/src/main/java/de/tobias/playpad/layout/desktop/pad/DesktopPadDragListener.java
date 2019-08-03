package de.tobias.playpad.layout.desktop.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.layout.desktop.DesktopEditMode;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutFactory;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
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
import java.util.Collection;
import java.util.Set;

public class DesktopPadDragListener implements EventHandler<DragEvent> {

	private static final String PADINDEX_DATATYPE = "de.tobias.playpad.padindex";
	private static final DataFormat dataFormat = new DataFormat(PADINDEX_DATATYPE);

	private Pad currentPad;
	private final Pane padView; // Node der PadView

	private DesktopMainLayoutFactory connect;

	private PadDragOptionView padHud;
	private FileDragOptionView fileHud;

	DesktopPadDragListener(Pad currentPad, IPadView view, DesktopMainLayoutFactory connect) {
		this.currentPad = currentPad;
		this.connect = connect;

		this.padView = view.getRootNode();
	}

	public void addListener() {
		this.padView.setOnDragOver(this::dragOver);
		this.padView.setOnDragExited(event -> dragExited());
		this.padView.setOnDragDropped(this::dragDropped);
		this.padView.setOnDragDetected(this::dragDetacted);
	}

	void removeListener() {
		this.padView.setOnDragOver(null);
		this.padView.setOnDragExited(null);
		this.padView.setOnDragDropped(null);
		this.padView.setOnDragDetected(null);
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
		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			return;
		}

		if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
			File file = event.getDragboard().getFiles().get(0);
			if (file.isFile()) {
				// Check Live Mode
				if (checkLiveMode()) {
					return;
				}

				// Build In Filesupport
				PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();
				Set<PadContentFactory> connects = registry.getPadContentConnectsForFile(file.toPath());

				if (!connects.isEmpty()) {
					if (fileHud == null) {
						fileHud = new FileDragOptionView(padView);
					}
					fileHud.showDropOptions(connects);

					event.acceptTransferModes(TransferMode.LINK);
					return;
				}
			}
		}

		// Drag and Drop von Pads
		if (event.getDragboard().hasContent(dataFormat)) {
			PadIndex index = (PadIndex) event.getDragboard().getContent(dataFormat);
			if (!currentPad.getPadIndex().equals(index)) {

				Collection<PadDragMode> connects = PlayPadPlugin.getRegistries().getDragModes().getComponents();

				if (!connects.isEmpty()) {
					if (padHud == null) {
						padHud = new PadDragOptionView(padView);
					}
					padHud.showDropOptions(connects);

					event.acceptTransferModes(TransferMode.MOVE);
				}
			}
		}
		event.consume();
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
		Project project = PlayPadPlugin.getInstance().getCurrentProject();

		Dragboard db = event.getDragboard();
		boolean success = false;

		// File Handling
		if (db.hasFiles()) {
			File file = db.getFiles().get(0);

			PadContentFactory connect = fileHud.getSelectedConnect();
			if (connect != null) {
				if (currentPad.getContent() == null || !currentPad.getContent().getType().equals(connect.getType())) {
					currentPad.setContentType(connect.getType());
				}

				if (currentPad.isPadVisible()) {
					currentPad.getController().getView().showBusyView(true);
				}

				this.currentPad.setPath(file.toPath());

				if (currentPad.getController() != null) {
					IPadView padView = currentPad.getController().getView();
					padView.setContentView(currentPad);
					padView.addDefaultElements(currentPad);
				}
			}
		}

		// Pad DnD
		if (db.hasContent(dataFormat)) {
			Object data = db.getContent(dataFormat);
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
		}
		// Event Completion
		event.setDropCompleted(success);
		event.consume();
	}

	private void dragDetacted(MouseEvent event) {
		if (connect.getEditMode() == DesktopEditMode.DRAG) {
			if (checkLiveMode()) {
				return;
			}

			Dragboard dragboard = padView.startDragAndDrop(TransferMode.MOVE);

			// Create Snapshot
			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			WritableImage snapshot = padView.snapshot(parameters, null);
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
			if (globalSettings.isLiveMode() && globalSettings.isLiveModeFile() && currentPad.getProject().getActivePlayers() > 0) {
				return true;
			}
		}
		return false;
	}
}
