package de.tobias.playpad.settings;

import de.tobias.utils.list.UniqList;

/**
 * Liste, wo nur ProfileRefernzen gespeichert werden, deren Namen Unique ist.
 * 
 * @author tobias
 *
 * @since 5.0.1
 * @see ProfileReference
 */
final class ProfileReferenceList extends UniqList<ProfileReference> {

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
				if (reference.getName() == o) {
					return true;
				} else if (reference.getName() == ((ProfileReference) o).getName()) {
					return true;
				}
			}
		}
		return super.contains(o);
	}
}