package de.tobias.playpad.project;

import de.tobias.utils.list.UniqList;

/**
 * Liste, wo nur ProjektRefernzen gespeichert werden, deren Namen Unique ist.
 * 
 * @author tobias
 *
 * @since 5.0.1
 * @see ProjectReference
 */
final class ProjectReferenceList extends UniqList<ProjectReference> {

	private static final long serialVersionUID = 1L;

	public boolean contains(Object o) {
		if (o instanceof String) {
			for (ProjectReference item : this) {
				if (item.getName().equals(o)) {
					return true;
				} else if (item.toString().equals(o)) {
					return true;
				}
			}
		} else if (o instanceof ProjectReference) {
			for (ProjectReference item : this) {
				if (item.getName() == o) {
					return true;
				} else {
					ProjectReference projectRef = (ProjectReference) o;
					if (item.getName().equals(projectRef.getName())) {
						return true;
					}
				}
			}
		}
		return super.contains(o);
	}
}