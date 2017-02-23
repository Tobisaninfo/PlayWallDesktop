package de.tobias.playpad.pad.drag;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;

public class ReplaceDragMode extends PadDragMode {

	public ReplaceDragMode(String type) {
		super(type);
	}

	@Override
	public boolean handle(PadIndex oldPad, PadIndex newPad, Project project) {
		Pad srcPad = project.getPad(oldPad);

		project.removePad(newPad);
		project.setPad(newPad, srcPad);
		return true;
	}

}
