package de.tobias.playpad.registry;

import de.tobias.playpad.plugin.Module;

import java.util.Collection;
import java.util.Set;

/**
 * Dieses Interface definiert Methoden bei Arbeit mit Komponenten, die durch Plugins zum Programm hinzugefügt werden können.
 * 
 * @author tobias
 *
 * @param <C>
 *            Item
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
	C getFactory(String id) throws NoSuchComponentException;

	/**
	 * Get a Components for a Class Type
	 * @param clazz type
	 * @return component
	 * @throws NoSuchComponentException no component found
	 */
	C getFactory(Class<?> clazz) throws NoSuchComponentException;

	/**
	 * Listet alle Type ID auf, die registriert wurden.
	 * 
	 * @return Liste mit IDs
	 */
	Set<String> getTypes();

	/**
	 * Listet alle Implementierungen auf.
	 * 
	 * @return Implementierungen
	 */
	Collection<C> getComponents();

	/**
	 * Gibt das Module zurück.
	 * 
	 * @param id
	 *            id der Komponente.
	 * @return Module
	 */
	Module getModule(String id);
}
