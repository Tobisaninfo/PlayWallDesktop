package de.tobias.playpad.registry;

/**
 * Zusätzliche Methoden Definitionen für eine Registry, die einen Default Wert braucht.
 *
 * @param <F> Type der Daten
 * @author tobias - s0553746
 */
public interface DefaultRegistry<F extends Component> extends Registry<F> {

	F getDefault();

	String getDefaultID();

	void setDefault(F component);

	void setDefaultID(String id) throws NoSuchComponentException;

	void setDefaultID(Class<?> clazz) throws NoSuchComponentException;

}
