package de.tobias.playpad.project.ref;

import de.tobias.utils.list.UniqList;

/**
 * Liste, wo nur ProjektRefernzen gespeichert werden, deren Namen Unique ist.
 *
 * @author tobias
 * @see ProjectReference
 * @since 5.0.1
 */
final class ProjectReferenceList extends UniqList<ProjectReference> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean contains(Object o) {
		if (o instanceof String) {
			for (ProjectReference reference : this) {
				if (reference.getName().equals(o)) {
					return true;
				} else if (reference.toString().equals(o)) {
					return true;
				}
			}
		} else if (o instanceof ProjectReference) {
			for (ProjectReference reference : this) {
				if (reference.getName() == o) {
					return true;
				} else if (reference.getName().equals(((ProjectReference) o).getName())) { // TODO Check
					return true;
				}
			}
		}
		return super.contains(o);
	}
}