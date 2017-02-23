package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.icon.FontIconType;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class ReplaceDragMode extends PadDragMode {

	public ReplaceDragMode(String type) {
		super(type);
	}

	@Override
	public boolean handle(PadIndex oldPad, PadIndex newPad, Project project) {
		Pad srcPad = project.getPad(oldPad);

		project.removePad(oldPad);
		project.setPad(newPad, srcPad);
		return true;
	}

}
