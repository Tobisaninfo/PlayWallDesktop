package de.tobias.playpad.server.sync.command;

/**
 * Created by tobias on 01.03.17.
 */
public class Change {

	private final String name;
	private final Object value;
	private final Object ref;

	public Change(String name, Object value, Object ref) {
		this.name = name;
		this.value = value;
		this.ref = ref;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public Object getRef() {
		return ref;
	}
}
