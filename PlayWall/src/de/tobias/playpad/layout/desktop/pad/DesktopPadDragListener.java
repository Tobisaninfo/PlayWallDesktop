package de.tobias.playpad.layout.desktop.pad;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.layout.desktop.DesktopEditMode;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutConnect;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadContentRegistry;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.view.PadDragOptionView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.FileUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class DesktopPadDragListener {

	private Pad sourcePad;
	final private Pane view;

	private DesktopMainLayoutConnect connect;
	private static Project project;

	private PadDragOptionView padHud;
	private FileDragOptionView fileHud;

	private static DataFormat dataFormat = new DataFormat("de.tobias.playpad.padindex");

	public DesktopPadDragListener(Pad pad, IPadView view, DesktopMainLayoutConnect connect) {
		this.sourcePad = pad;
		this.connect = connect;

		this.view = view.getRootNode();

		// Drag and Drop
		this.view.setOnDragOver(event -> dragOver(event));
		this.view.setOnDragExited(event -> dragExited());
		this.view.setOnDragDropped(event -> dragDropped(event));
		this.view.setOnDragDetected(event -> dragDetacted(event));
	}

	private void dragOver(DragEvent event) {
		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			return;
		}

		if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
			if (event.getDragboard().getFiles().get(0).isFile()) {

				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

				if (sourcePad.getProject() != null) {
					if (globalSettings.isLiveMode() && globalSettings.isLiveModeFile() && sourcePad.getProject().getActivePlayers() > 0) {
						return;
					}
				}

				File file = event.getDragboard().getFiles().get(0);

				// Build In Filesupport
				try {
					PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();
					Set<PadContentConnect> connects = registry.getPadContentConnectsForFile(file.toPath());

					if (!connects.isEmpty()) {
						if (fileHud == null) {
							fileHud = new FileDragOptionView(view);
						}
						fileHud.showDropOptions(connects);

						event.acceptTransferModes(TransferMode.LINK);
						return;
					}
				} catch (NoSuchComponentException e) {
					e.printStackTrace();
				}
			}
		}

		// Drag and Drop von Pads
		if (event.getDragboard().hasContent(dataFormat)) {
			PadIndex index = (PadIndex) event.getDragboard().getContent(dataFormat); // TODO Check cast
			if (!sourcePad.getPadIndex().equals(index)) {

				Collection<PadDragMode> connects = PlayPadPlugin.getRegistryCollection().getDragModes().getComponents();

				if (!connects.isEmpty()) {
					if (padHud == null) {
						padHud = new PadDragOptionView(view);
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

	private void dragDropped(DragEvent event) {
		Dragboard db = event.getDragboard();
		boolean success = false;
		if (db.hasFiles()) {
			success = true;
			File file = db.getFiles().get(0);

			PadContentConnect connect = fileHud.getSelectedConnect();
			if (connect != null) {
				PadContent content = sourcePad.getContent();
				if (sourcePad.getContent() == null || !sourcePad.getContent().getType().equals(connect.getType())) {
					content = connect.newInstance(sourcePad);
				}

				try {
					content.handlePath(file.toPath());
				} catch (NoSuchComponentException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.sourcePad.setContent(content);
				this.sourcePad.setName(FileUtils.getFilenameWithoutExtention(file.toPath().getFileName()));

				if (sourcePad.getController() != null) {
					IPadView padView = sourcePad.getController().getView();
					padView.setContentView(sourcePad);
					padView.addDefaultElement(sourcePad);
				}
			}
		}

		if (db.hasContent(dataFormat)) {
			PadIndex padID = (PadIndex) db.getContent(dataFormat); // TODO Check Cast

			PadDragMode mode = padHud.getSelectedPadDragMode();

			mode.handle(padID, sourcePad.getPadIndex(), project);
			padHud.hide();

			IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();
			mainViewController.showPage(mainViewController.getPage());

			event.setDropCompleted(success);
			event.consume();
		}
	}

	private void dragDetacted(MouseEvent event) {
		if (connect.getEditMode() == DesktopEditMode.DRAG) {
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

			if (sourcePad.getProject() != null) {
				if (globalSettings.isLiveMode() && globalSettings.isLiveModeDrag() && sourcePad.getProject().getActivePlayers() > 0) {
					return;
				}
			}

			Dragboard dragboard = view.startDragAndDrop(TransferMode.MOVE);

			SnapshotParameters parameters = new SnapshotParameters();
			parameters.setFill(Color.TRANSPARENT);
			WritableImage snapshot = view.snapshot(parameters, null);
			for (int x = 0; x < snapshot.getWidth(); x++) {
				for (int y = 0; y < snapshot.getHeight(); y++) {
					Color oldColor = snapshot.getPixelReader().getColor(x, y).darker().darker();
					Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity() * 0.5);
					snapshot.getPixelWriter().setColor(x, y, newColor);
				}
			}

			dragboard.setDragView(snapshot);

			ClipboardContent content = new ClipboardContent();
			content.put(dataFormat, sourcePad.getPadIndex());
			dragboard.setContent(content);

			event.consume();
		}
	}

	public static void setProject(Project project) {
		DesktopPadDragListener.project = project;
	}

}
