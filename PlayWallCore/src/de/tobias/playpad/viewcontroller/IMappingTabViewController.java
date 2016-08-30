package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.action.Action;

/**
 * Schnittstelle für den Mapping Tab ViewController. Der Controller hat zwei Bereiche: ActionType (Optional), Action
 * 
 * @author tobias
 *
 * @since 5.0.0
 */
public interface IMappingTabViewController {

	/**
	 * Zeigt im Action Teil der View die Einstellungen zu einer Action an.
	 * 
	 * @param action
	 *            Action
	 */
	public void showMapperFor(Action action);
}
