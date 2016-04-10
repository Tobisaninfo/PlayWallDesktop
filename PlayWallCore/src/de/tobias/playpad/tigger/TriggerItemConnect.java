package de.tobias.playpad.tigger;

import de.tobias.utils.ui.ContentViewController;

public abstract class TriggerItemConnect {

	public abstract String getType();

	public abstract TriggerItem newInstance(Trigger trigger);

	public abstract ContentViewController getSettingsController(TriggerItem item);

}
