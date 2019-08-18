package de.tobias.playpad.pad.drag;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;

public class DuplicateDragMode extends PadDragMode {

	public DuplicateDragMode(String type) {
		super(type);
	}

	@Override
	public boolean handle(PadIndex oldIndex, PadIndex newIndex, Project project) {
		Pad oldPad = project.getPad(oldIndex);
		Pad newPad = project.getPad(newIndex);

		try {
			Pad copyPad = oldPad.clone(oldPad.getPage());

			project.removePad(newPad.getUuid());
			project.setPad(newIndex, copyPad);
			return true;
		} catch (CloneNotSupportedException e) {
			Logger.error(e);
		}
		return false;
	}
}