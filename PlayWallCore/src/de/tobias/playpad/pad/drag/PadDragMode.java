package de.tobias.playpad.pad.drag;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.Component;

/**
 * Modus um ein Pad mit Drag and Drop zu verschieben.
 * 
 * @author tobias
 *
 * @since 6.0.0
 */
public abstract class PadDragMode extends Component implements Comparable<PadDragMode> {

	public PadDragMode(String type) {
		super(type);
	}

	/**
	 * Führt die Drag and Drop Aktion aus, ändert das Datenmodell.
	 * 
	 * @param oldPad
	 *            Alter Index
	 * @param newPad
	 *            Neuer Index
	 * @param project
	 *            Projekt zu den Pads
	 * @return <code>true</code> Erfolgreiches DnD
	 */
	public abstract boolean handle(PadIndex oldPad, PadIndex newPad, Project project);

	@Override
	public int compareTo(PadDragMode o) {
		return getType().compareTo(o.getType());
	}
}
