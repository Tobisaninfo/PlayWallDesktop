package de.tobias.playpad.plugin;

import de.tobias.playpad.project.Project;

public interface GlobalListener {

	void projectOpened(Project newProject);

	void projectClosed(Project currentProject);
}
