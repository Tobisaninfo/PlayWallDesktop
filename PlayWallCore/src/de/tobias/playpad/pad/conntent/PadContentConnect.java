package de.tobias.playpad.pad.conntent;

import java.nio.file.Path;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.utils.util.FileUtils;
import javafx.scene.layout.Pane;

// COMMENT PadContentConnect
public abstract class PadContentConnect implements Comparable<PadContentConnect>, Displayable {

	public abstract String getType();

	public abstract PadContent newInstance(Pad pad);

	public abstract IPadContentView getPadContentPreview(Pad pad, Pane parentNode);

	public SettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		return null;
	}

	public PadSettingsTabViewController getSettingsViewController(Pad pad) {
		return null;
	}

	public abstract String[] getSupportedTypes();

	@Override
	public int compareTo(PadContentConnect o) {
		return getType().compareTo(o.getType());
	}

	public static boolean isFileSupported(Path path, PadContentConnect connect) {
		String extension = FileUtils.getFileExtention(path);
		for (String ex : connect.getSupportedTypes()) {
			if (ex.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}
}
