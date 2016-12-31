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

public class MoveDragMode extends PadDragMode {

	public MoveDragMode(String type) {
		super(type);
	}

	@Override
	public boolean handle(PadIndex oldIndex, PadIndex newIndex, Project project) {
		Pad oldPad = project.getPad(oldIndex);
		Pad newPad = project.getPad(newIndex);

		// Alte Pads entfernen, damit keine Nebenabhängigkeiten entstehen in den verschiedenen Seiten
		project.setPad(oldIndex, null);
		project.setPad(newIndex, null);

		// Neue Pads in die Seiten einfügen
		project.setPad(oldIndex, newPad);
		project.setPad(newIndex, oldPad);
		return true;
	}

}
