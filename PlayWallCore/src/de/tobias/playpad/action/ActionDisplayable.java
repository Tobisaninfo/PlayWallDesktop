package de.tobias.playpad.action;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.utils.ui.ContentViewController;

public interface ActionDisplayable extends Displayable {

	public default ContentViewController getActionSettingsViewController(Mapping mapping, IMappingTabViewController controller) {
		return null;
	}
}
