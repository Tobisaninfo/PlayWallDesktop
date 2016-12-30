package de.tobias.playpad.plugin;

/**
 * Ein Modul beschreibt ein Plugin. Es wird verwendet, um Components der Registry einem Mpdul zuzuordnen.
 * 
 * @author tobias - s0553746
 *
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
