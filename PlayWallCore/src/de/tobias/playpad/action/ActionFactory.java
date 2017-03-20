package de.tobias.playpad.action;

import java.util.List;

import de.tobias.playpad.registry.Component;
import de.tobias.playpad.profile.Profile;
import javafx.scene.control.TreeItem;

/**
 * Verwalter einer Action für die Instance und GUI.
 * 
 * @author tobias
 *
 * @since 5.0.0
 * 
 * @see Action Implementierung der eigentlichen Action.
 */
public abstract class ActionFactory extends Component {

	public ActionFactory(String type) {
		super(type);
	}

	/**
	 * Erstellt ein TreeItem für die Grupierung der Actions.
	 * 
	 * @param actions
	 *            Liste der alle Actionen dieses Types.
	 * @param mapping
	 *            Mapping, indem die Actions sind.
	 * @return TreeItem
	 */
	public abstract TreeItem<ActionDisplayable> getTreeViewForActions(List<Action> actions, Mapping mapping);

	/**
	 * Initialisiert ein Mapping mit allen möglichen Action Varianten. Dies ist nötig, damit alle möglichen Actions in den Einstellungen
	 * aufgelistet werden können.
	 * 
	 * @param mapping
	 *            Mapping
	 * @param profile
	 *            Profile für Einstellungen
	 */
	public abstract void initActionType(Mapping mapping, Profile profile);

	/**
	 * Erstellt eine neue Instance der Aktion.
	 * 
	 * @return Neue Instance.
	 */
	public abstract Action newInstance();

	/**
	 * Gibt die Art der Action zurück. Gibt Auskunft über die Anordnung in den Einstellungen.
	 * 
	 * @return Art der Aktion
	 * 
	 * @see ActionType
	 */
	public abstract ActionType geActionType();

}
