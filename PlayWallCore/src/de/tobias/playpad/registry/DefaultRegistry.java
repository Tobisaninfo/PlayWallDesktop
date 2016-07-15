package de.tobias.playpad.registry;


public interface DefaultRegistry<C> extends Registry<C> {

	public C getDefault();
	
	public void setDefaultID(String id) throws NoSuchComponentException;
	
	public void setDefault(C component);
}
