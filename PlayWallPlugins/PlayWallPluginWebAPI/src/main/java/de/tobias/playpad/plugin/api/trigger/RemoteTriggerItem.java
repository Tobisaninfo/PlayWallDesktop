package de.tobias.playpad.plugin.api.trigger;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class RemoteTriggerItem extends TriggerItem {

	private final String type;

	public RemoteTriggerItem(String type) {
		this.type = type;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void performAction(Pad pad, Project project, IMainViewController controller, Profile profile) {

	}

	@Override
	public TriggerItem copy() {
		return null;
	}
}
