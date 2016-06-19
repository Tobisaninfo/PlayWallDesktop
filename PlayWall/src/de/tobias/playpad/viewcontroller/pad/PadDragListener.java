package de.tobias.playpad.viewcontroller.pad;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.pad.drag.PadDragModeRegistery;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.FileDragOptionView;
import de.tobias.playpad.view.PadDragOptionView;
import de.tobias.playpad.view.PadView;
import de.tobias.utils.util.FileUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

public class PadDragListener {

	private static final String REGEX = "[0-9]+";
	private Pad pad;
	final private PadView view;

	private static boolean dndMode;
	private static Project project;

	private PadDragOptionView padHud;
	private FileDragOptionView fileHud;

	public PadDragListener(Pad pad, PadView view) {
		this.pad = pad;
		this.view = view;

		// Drag and Drop
		view.setOnDragOver(event -> dragOver(event));
		view.setOnDragExited(event -> dragExited());
		view.setOnDragDropped(event -> dragDropped(event));
		view.setOnDragDetected(event -> dragDetacted(event));
	}

	private void dragOver(DragEvent event) {
		if (Profile.currentProfile().getProfileSettings().isLocked()) {
			return;
		}

		if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
			if (event.getDragboard().getFiles().get(0).isFile()) {
				ProfileSettings settings = Profile.currentProfile().getProfileSettings();
				if (pad.getProject() != null) {
					if (settings.isLiveMode() && settings.isLiveModeFile() && pad.getProject().getPlayedPlayers() > 0) {
						PlayPadPlugin.getImplementation().getMainViewController().showLiveInfo();
						return;
					}
				}
				
				File file = event.getDragboard().getFiles().get(0);

				// Build In Filesupport
				try {
					Set<PadContentConnect> connects = PadContentRegistry.getPadContentConnectsForFile(file.toPath());

					if (!connects.isEmpty()) {
						if (fileHud == null) {
							fileHud = new FileDragOptionView(view);
						}
						fileHud.showDropOptions(connects);

						event.acceptTransferModes(TransferMode.LINK);
						return;
					}
				} catch (UnkownPadContentException e) {
					e.printStackTrace();
				}
			}
		}

		// Drag and Drop von Pads
		if (event.getDragboard().hasString() && event.getDragboard().getString().trim().matches(REGEX)) {
			int padID = Integer.valueOf(event.getDragboard().getString());
			if (padID != view.getController().getPad().getIndex()) {

				Collection<PadDragMode> connects = PadDragModeRegistery.getValues();

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
				PadContent content = pad.getContent();
				if (pad.getContent() == null || !pad.getContent().getType().equals(connect.getType())) {
					content = connect.newInstance(pad);
				}

				content.handlePath(file.toPath());
				this.pad.setContent(content);
				this.pad.setName(FileUtils.getFilenameWithoutExtention(file.toPath().getFileName()));

				view.setPreviewContent(pad);
				view.addDefaultButton(pad);
			}
		}

		if (db.hasString() && db.getString().matches(REGEX)) {
			int padID = Integer.valueOf(db.getString());

			PadDragMode mode = padHud.getSelectedPadDragMode();
			mode.handle(padID, pad.getIndex(), project);
			padHud.hide();

			PlayPadPlugin.getImplementation().getMainViewController()
					.showPage(PlayPadPlugin.getImplementation().getMainViewController().getPage());

			event.setDropCompleted(success);
			event.consume();
		}
	}

	private void dragDetacted(MouseEvent event) {
		if (dndMode) {
			ProfileSettings settings = Profile.currentProfile().getProfileSettings();
			if (pad.getProject() != null) {
				if (settings.isLiveMode() && settings.isLiveModeDrag() && pad.getProject().getPlayedPlayers() > 0) {
					PlayPadPlugin.getImplementation().getMainViewController().showLiveInfo();
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
					snapshot.getPixelWriter().setColor(x, y,
							new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), oldColor.getOpacity() * 0.5));
				}
			}

			dragboard.setDragView(snapshot);

			ClipboardContent content = new ClipboardContent();
			content.putString(String.valueOf(pad.getIndex()));
			dragboard.setContent(content);

			event.consume();
		}
	}

	public static void setDndMode(boolean dndMode) {
		PadDragListener.dndMode = dndMode;
	}

	public void setPad(Pad pad) {
		this.pad = pad;
	}

	public static void setProject(Project project) {
		PadDragListener.project = project;
	}

}
