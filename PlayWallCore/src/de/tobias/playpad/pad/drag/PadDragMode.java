package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.v2.ProjectV2;

/**
 * Modus um ein Pad mit Drag and Drop zu verschieben.
 * 
 * @author tobias
 *
 * @since 6.0.0
 */
public abstract class PadDragMode implements Displayable, Comparable<PadDragMode> {

	public abstract String getType();

	public abstract void handle(PadIndex oldPad, PadIndex newPad, ProjectV2 project);

	@Override
	public int compareTo(PadDragMode o) {
		return getType().compareTo(o.getType());
	}
}
