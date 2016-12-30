package de.tobias.playpad.project;

import de.tobias.playpad.project.ref.ProjectReference;

public class ProjectNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private ProjectReference ref;

	public ProjectNotFoundException(ProjectReference ref) {
		super(ref.getName());
		this.ref = ref;
	}

	public ProjectReference getRef() {
		return ref;
	}
}
