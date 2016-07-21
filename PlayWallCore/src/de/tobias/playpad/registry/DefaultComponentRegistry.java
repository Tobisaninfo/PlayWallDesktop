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
	public String getDefaultID() {
		for (String type : getTypes()) {
			try {
				if (getComponent(type).equals(defaultValue)) {
					return type;
				}
			} catch (NoSuchComponentException e) {
				// Exception will never been thrown, because all elements (getTypes()) exists. Otherwise something is totally wrong.
			}
		}
		return null;
	}

	@Override
	public void setDefault(C component) {
		this.defaultValue = component;
	}

	@Override
	public void setDefaultID(String id) throws NoSuchComponentException {
		setDefault(getComponent(id));
	}

}
