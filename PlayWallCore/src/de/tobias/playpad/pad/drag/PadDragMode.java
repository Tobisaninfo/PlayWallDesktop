package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.project.Project;

public abstract class PadDragMode implements Displayable, Comparable<PadDragMode> {

	public abstract String getType();

	public abstract void handle(int oldPad, int newPad, Project project);

	@Override
	public int compareTo(PadDragMode o) {
		return getType().compareTo(o.getType());
	}
}
