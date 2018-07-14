package de.tobias.playpad.pad.drag;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;

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
