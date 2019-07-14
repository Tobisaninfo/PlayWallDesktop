package de.tobias.playpad.action;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;

/**
 * Einn zusätzliches Interface für die Klasse {@link ActionProvider} oder {@link Action} mit der es möglich ist für ein ActionType oder eine
 * Action Einstellungen anzuzeigen.
 *
 * @author tobias
 * @since 5.0.0
 */
public interface ActionDisplayable extends Displayable {

	/**
	 * Provide a general settings controller for type of actions
	 *
	 * @param mapping    current mapping
	 * @param controller current
	 * @return ViewController für den ActionType
	 */
	default NVC getActionSettingsViewController(Mapping mapping, IMappingTabViewController controller) {
		return null;
	}
}
