package de.tobias.playpad.pad.content;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.drag.ContentDragOption;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.scene.layout.Pane;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public abstract class PadContentFactory extends Component implements ContentDragOption {

	public interface PadContentTypeChooser {
		void showOptions(Collection<PadContentFactory> options, Consumer<PadContentFactory> onSelected);
	}

	public PadContentFactory(String type) {
		super(type);
	}

	public abstract PadContent newInstance(Pad pad);

	public abstract IPadContentView getPadContentPreview(Pad pad, Pane parentNode);

	public ProfileSettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		return null;
	}

	public PadSettingsTabViewController getSettingsViewController(Pad pad) {
		return null;
	}

	public abstract String[] getSupportedTypes();

	public static boolean isFileTypeSupported(Path path, PadContentFactory connect) {
		String extension = PathUtils.getFileExtension(path);
		for (String ex : connect.getSupportedTypes()) {
			if (ex.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	// Generic Drag Option for all content types

	@Override
	public void handleDrop(Pad currentPad, List<File> files) {
		if (currentPad.getContent() == null || !currentPad.getContent().getType().equals(getType())) {
			currentPad.setContentType(getType());
		}

		if (currentPad.isPadVisible()) {
			currentPad.getController().getView().showBusyView(true);
		}

		currentPad.setPath(files.get(0).toPath());
	}
}
