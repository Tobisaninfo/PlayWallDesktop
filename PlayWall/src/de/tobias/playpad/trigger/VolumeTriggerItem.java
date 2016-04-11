package de.tobias.playpad.trigger;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class VolumeTriggerItem extends TriggerItem {

	@Override
	public String getType() {
		return VolumeTriggerItemConnect.TYPE;
	}

	@Override
	public void performAction(Pad pad, Project project, IMainViewController controller, Profile profile) {

	}
}
