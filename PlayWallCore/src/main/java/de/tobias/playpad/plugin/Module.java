package de.tobias.playpad.plugin;

import java.util.Objects;

/**
 * Definition a plugin module content. Plugin components are registered with its module.
 *
 * @author tobias
 */
public class Module {

	public final String name;
	public final String identifier;

	public Module(String name, String identifier) {
		this.name = name;
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "Module [name=" + name + ", identifier=" + identifier + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Module)) return false;
		Module module = (Module) o;
		return Objects.equals(identifier, module.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier);
	}
}
