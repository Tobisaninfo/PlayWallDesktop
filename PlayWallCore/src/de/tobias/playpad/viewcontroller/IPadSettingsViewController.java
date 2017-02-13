package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.pad.Pad;

public interface IPadSettingsViewController {

	Pad getPad();

	void addTab(PadSettingsTabViewController controller);

}
