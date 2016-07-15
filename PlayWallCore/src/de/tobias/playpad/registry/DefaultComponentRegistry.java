package de.tobias.playpad.registry;

public class DefaultComponentRegistry<C> extends ComponentRegistry<C> implements DefaultRegistry<C> {

	private C defaultValue;
	
	public DefaultComponentRegistry(String name) {
		super(name);
	}

	@Override
	public C getDefault() {
		return defaultValue;
	}

	@Override
	public void setDefaultID(String id) throws NoSuchComponentException {
		setDefault(getComponent(id));
	}
	
	@Override
	public void setDefault(C component) {
		this.defaultValue = component;
	}

}
