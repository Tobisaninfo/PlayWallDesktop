package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.pad.Pad;

public interface IPadSettingsViewController {

	public Pad getPad();

	public void addTab(PadSettingsTabViewController controller);

}
