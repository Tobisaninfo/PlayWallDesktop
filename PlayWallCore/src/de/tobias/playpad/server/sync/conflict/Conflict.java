package de.tobias.playpad.server.sync.conflict;

import de.tobias.playpad.project.ref.ProjectReference;

import java.util.List;

/**
 * Created by tobias on 30.03.17.
 */
public class Conflict {
	private final ProjectReference projectReference;
	private final ConflictType conflictType;
	private final List<Version> version;

	public Conflict(ProjectReference projectReference, ConflictType conflictType, List<Version> version) {
		this.projectReference = projectReference;
		this.conflictType = conflictType;
		this.version = version;
	}

	public ProjectReference getProjectReference() {
		return projectReference;
	}

	public ConflictType getConflictType() {
		return conflictType;
	}

	public List<Version> getVersion() {
		return version;
	}
}
