package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.pad.Pad;
import de.tobias.utils.ui.ContentViewController;

public abstract class PadSettingsTabViewController extends ContentViewController {

	public PadSettingsTabViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	public abstract String getName();

	public abstract void loadSettings(Pad pad);

	public abstract void saveSettings(Pad pad);
}
