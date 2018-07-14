package de.tobias.playpad.server.sync.listener.upstream;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.value.ChangeListener;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class ProjectUpdateListener {

	private Project project;

	private ChangeListener<String> nameListener;

	public ProjectUpdateListener(Project project) {
		this.project = project;

		nameListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAGE_POSITION, newValue, project);
			CommandManager.execute(Commands.PAGE_UPDATE, project.getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			project.getProjectReference().nameProperty().addListener(nameListener);
		}
	}

	public void removeListener() {
		added = false;
		project.getProjectReference().nameProperty().addListener(nameListener);
	}
}
