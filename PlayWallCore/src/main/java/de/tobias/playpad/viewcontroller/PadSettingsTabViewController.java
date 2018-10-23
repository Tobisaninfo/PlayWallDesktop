package de.tobias.playpad.viewcontroller;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.pad.Pad;

public abstract class PadSettingsTabViewController extends NVC {

	public PadSettingsTabViewController() {

	}

	public abstract String getName();

	public abstract void loadSettings(Pad pad);

	public abstract void saveSettings(Pad pad);
}
