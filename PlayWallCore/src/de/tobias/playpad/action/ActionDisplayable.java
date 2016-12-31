package de.tobias.playpad.action;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.utils.ui.ContentViewController;

/**
 * Einn zusätzliches Interface für die Klasse {@link ActionFactory} oder {@link Action} mit der es möglich ist für ein ActionType oder eine
 * Action Einstellungen anzuzeigen.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
// TODO Redo
public interface ActionDisplayable extends Displayable {

	/**
	 * Erlaubt es einen ViewController für diesen ActionType oder die Action zu schalten.
	 * 
	 * @param mapping
	 *            Aktuelles Mapping
	 * @param controller
	 *            Aktueller ViewController für das Mapping
	 * @return ViewController für den ActionType
	 */
	public default ContentViewController getActionSettingsViewController(Mapping mapping, IMappingTabViewController controller) {
		return null;
	}
}
