package de.tobias.playpad.trigger;

import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.tigger.TriggerItemConnect;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;

public class VolumeTriggerItemConnect extends TriggerItemConnect {

	public final static String TYPE = "Volume";
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public TriggerItem newInstance(Trigger trigger) {
		return new VolumeTriggerItem();
	}

	@Override
	public ContentViewController getSettingsController(TriggerItem item) {
		return null;
	}

	@Override
	public String toString() {
		return Localization.getString(Strings.Trigger_Volume_Name);
	}
}
