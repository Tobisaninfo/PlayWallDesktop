package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.ServerUtils;
import de.tobias.playpad.server.sync.PropertyDef;
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
			if (ServerUtils.isNewValueComingFromServer()) {
				return;
			}
			JsonObject json = new JsonObject();
			json.addProperty(PropertyDef.ID, project.getProjectReference().getUuid().toString());
			json.addProperty(PropertyDef.FIELD, PropertyDef.PROJECT_NAME);

			json.addProperty(PropertyDef.VALUE, project.getProjectReference().getName());
			json.addProperty(PropertyDef.CMD, Commands.PROJECT_UPDATE);

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.push(json);
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
