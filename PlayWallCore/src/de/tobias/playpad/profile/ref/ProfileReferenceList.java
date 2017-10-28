package de.tobias.playpad.profile.ref;

import de.tobias.utils.list.UniqList;

import java.util.Collection;

/**
 * Liste, wo nur ProfileRefernzen gespeichert werden, deren Namen Unique ist.
 * 
 * @author tobias
 *
 * @since 5.0.1
 * @see ProfileReference
 */
public final class ProfileReferenceList extends UniqList<ProfileReference> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean contains(Object o) {
		if (o instanceof String) {
			for (ProfileReference reference : this) {
				if (reference.getName().equals(o)) {
					return true;
				} else if (reference.toString().equals(o)) {
					return true;
				}
			}
		} else if (o instanceof ProfileReference) {
			for (ProfileReference reference : this) {
				if (reference == o) {
					return true;
				} else if (reference.getName().equals(((ProfileReference) o).getName())) {
					return true;
				}
			}
		}
		return super.contains(o);
	}

	public void setAll(Collection<ProfileReference> elements) {
		clear();
		addAll(elements);
	}

	public boolean containsProfileName(String name) {
		return stream().anyMatch(profile -> profile.getName().equals(name));
	}
}