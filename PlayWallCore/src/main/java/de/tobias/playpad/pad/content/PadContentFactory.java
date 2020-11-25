package de.tobias.playpad.pad.content;

import de.thecodelabs.utils.io.PathUtils;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.scene.layout.Pane;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

// COMMENT PadContentFactory
public abstract class PadContentFactory extends Component implements Comparable<PadContentFactory> {

	public interface PadContentTypeChooser {
		void showOptions(Set<PadContentFactory> options, Consumer<PadContentFactory> onSelected);
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

	@Override
	public int compareTo(PadContentFactory o) {
		return getType().compareTo(o.getType());
	}

	public static boolean isFileTypeSupported(Path path, PadContentFactory connect) {
		String extension = PathUtils.getFileExtension(path);
		for (String ex : connect.getSupportedTypes()) {
			if (ex.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}
