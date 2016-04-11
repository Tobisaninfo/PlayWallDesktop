package de.tobias.playpad.viewcontroller.pad;

import java.io.File;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.plugin.ExtensionHandler;
import de.tobias.playpad.plugin.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.view.AudioPadView;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.Refreshable;
import de.tobias.utils.util.Localization;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

// TODO Renew class
public class PadDragHandler<T extends Refreshable & NotificationHandler> {

	private static final String REGEX = "[0-9]+";
	private Pad pad;
	final private AudioPadView view;
	final private T t;

	private static AudioPadView lastDraggedOver;
	private static boolean dndMode;
	private static Project project;

	public PadDragHandler(Pad pad, AudioPadView view, T t) {
		this.pad = pad;
		this.view = view;
		this.t = t;

		// Drag and Drop
		view.setOnDragOver(event -> dragOver(event));
		view.setOnDragExited(event -> dragExited());
		view.setOnDragDropped(event -> dragDropped(event));
		view.setOnDragDetected(event -> dragDetacted(event));
	}

	private void dragOver(DragEvent event) {
		if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
			if (event.getDragboard().getFiles().get(0).isFile()) {
				if (PadViewController.getPlayedPlayers() > 0) {
					showLiveModeLabel();
				}
				File file = event.getDragboard().getFiles().get(0);

				// Build In Filesupport
				for (String extension : AudioRegistry.geAudioType().getSupportedTypes()) {
					if (file.getName().toLowerCase().matches("." + extension)) {
						event.acceptTransferModes(TransferMode.LINK);
						return;
					}
				}

				// Plugins
				for (ExtensionHandler extensionHandler : PlayPadPlugin.getImplementation().getExtensionsHandler()) {
					for (String extension : extensionHandler.getExtensions()) {
						if (file.getName().toLowerCase().matches("." + extension)) {
							event.acceptTransferModes(TransferMode.LINK);
							return;
						}
					}
				}
			}
		}

		if (event.getDragboard().hasString() && event.getDragboard().getString().trim().matches(REGEX)) {
			int padID = Integer.valueOf(event.getDragboard().getString());
			if (padID != view.getController().getPad().getIndex()) {

				// Live Mode
				if (PadViewController.getPlayedPlayers() > 0) {
					showLiveModeLabel();
				} else {
					event.acceptTransferModes(TransferMode.MOVE);
					double y = event.getY();
					if (y < view.getHeight() / 2) {
						if (lastDraggedOver != null) {
							view.pseudoClassState(PseudoClasses.DRAG_RELACE_CLASS, false);
							view.pseudoClassState(PseudoClasses.DRAG_MOVE_CLASS, false);
						}
						view.pseudoClassState(PseudoClasses.DRAG_RELACE_CLASS, false);
						view.pseudoClassState(PseudoClasses.DRAG_MOVE_CLASS, true);
						lastDraggedOver = view;
					} else {
						if (lastDraggedOver != null) {
							view.pseudoClassState(PseudoClasses.DRAG_RELACE_CLASS, false);
							view.pseudoClassState(PseudoClasses.DRAG_MOVE_CLASS, false);
						}
						view.pseudoClassState(PseudoClasses.DRAG_MOVE_CLASS, false);
						view.pseudoClassState(PseudoClasses.DRAG_RELACE_CLASS, true);
						lastDraggedOver = view;
					}
				}
			}
		}
		event.consume();
	}

	private void dragExited() {
		if (lastDraggedOver != null) {
			view.pseudoClassState(PseudoClasses.DRAG_RELACE_CLASS, false);
			view.pseudoClassState(PseudoClasses.DRAG_MOVE_CLASS, false);
			lastDraggedOver = null;
		}
	}

	private void dragDropped(DragEvent event) {
		// Live Mode
		if (PadViewController.getPlayedPlayers() > 0) {
			showLiveModeLabel();
		} else {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles()) {
				success = true;
				File file = db.getFiles().get(0);

				boolean custom = false;
				for (ExtensionHandler extensionHandler : PlayPadPlugin.getImplementation().getExtensionsHandler()) {
					for (String extension : extensionHandler.getExtensions()) {
						if (file.getName().toLowerCase().matches("." + extension)) {
							extensionHandler.handle(file.toPath(), pad);
							custom = true;
							break;
						}
					}
				}
				if (!custom) {
					this.pad.setPath(file.toPath());
				}
			}

			if (db.hasString() && db.getString().matches(REGEX)) {
				double y = event.getY();
				if (y < view.getHeight() / 2) {
					int padID = Integer.valueOf(db.getString());
					project.movePads(padID, pad.getIndex());
					t.refreshUI();
					success = true;
				} else {
					int padID = Integer.valueOf(db.getString());
					project.replacePads(padID, pad.getIndex());
					t.refreshUI();
					success = true;
				}
			}

			event.setDropCompleted(success);
			event.consume();
		}
	}

	private void dragDetacted(MouseEvent event) {
		// Live Mode
		if (PadViewController.getPlayedPlayers() == 0) {
			if (dndMode) {
				Dragboard storeImage = view.startDragAndDrop(TransferMode.MOVE);
				storeImage.setDragView(view.snapshot(new SnapshotParameters(), null));

				ClipboardContent content = new ClipboardContent();
				content.putString(String.valueOf(pad.getIndex()));
				storeImage.setContent(content);

				event.consume();
			}
		}
	}

	public static void setDndMode(boolean dndMode) {
		PadDragHandler.dndMode = dndMode;
	}

	public static void setLastDraggedOver(AudioPadView lastDraggedOver) {
		PadDragHandler.lastDraggedOver = lastDraggedOver;
	}

	public void setPad(Pad pad) {
		this.pad = pad;
	}

	public static void setProject(Project project) {
		PadDragHandler.project = project;
	}

	private boolean displayLiveLabel;

	private synchronized void showLiveModeLabel() {
		if (!displayLiveLabel) {
			displayLiveLabel = true;
			t.notify(Localization.getString(Strings.Error_Pad_Livemode), PlayPadMain.notificationDisplayTimeMillis,
					() -> displayLiveLabel = false);
		}
	}
}
