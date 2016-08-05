package de.tobias.playpad.registry;

/**
 * Zusätzliche Methoden Definitionen für eine Registry, die einen Default Wert braucht.
 * 
 * @author tobias - s0553746
 *
 * @param <C> Type der Daten
 */
public interface DefaultRegistry<C> extends Registry<C> {

	public C getDefault();

	public String getDefaultID();

	public void setDefault(C component);

	public void setDefaultID(String id) throws NoSuchComponentException;

}
