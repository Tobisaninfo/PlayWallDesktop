package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.pad.Pad;
import de.tobias.utils.ui.NVC;

public abstract class PadSettingsTabViewController extends NVC {

	public PadSettingsTabViewController() {

	}

	public abstract String getName();

	public abstract void loadSettings(Pad pad);

	public abstract void saveSettings(Pad pad);
}
