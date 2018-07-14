package de.tobias.playpad.pad.content;

import java.nio.file.Path;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.util.FileUtils;
import javafx.scene.layout.Pane;

// COMMENT PadContentFactory
public abstract class PadContentFactory extends Component implements Comparable<PadContentFactory> {

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
		String extension = FileUtils.getFileExtention(path);
		for (String ex : connect.getSupportedTypes()) {
			if (ex.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}
