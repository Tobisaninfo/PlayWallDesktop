package de.tobias.playpad.registry;

import java.util.Collection;
import java.util.Set;

/**
 * Dieses Interface definiert Methoden bei Arbeit mit Komponenten, die durch Plugins zum Programm hinzugefügt werden können.
 * 
 * @author tobias
 *
 * @param <C>
 *            Component
 * 
 * @since 5.1.0
 */
public interface Registry<C> extends WriteOnlyRegistry<C> {

	/**
	 * Gibt eine Komponenten zu einer ID zurück.
	 * 
	 * @param id
	 *            ID der Komponenten
	 * @return Komponente
	 * 
	 * @throws NoSuchComponentException
	 *             Wird geworfen, wenn die Komponente nicht existiert.
	 */
	public C getComponent(String id) throws NoSuchComponentException;

	/**
	 * Listet alle Type ID auf, die registriert wurden.
	 * 
	 * @return Liste mit IDs
	 */
	public Set<String> getTypes();

	/**
	 * Listet alle Implementierungen auf.
	 * 
	 * @return Implementierungen
	 */
	public Collection<C> getComponents();
}
