package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.ListenerUtils;
import de.tobias.playpad.server.sync.listener.PropertyDef;
import javafx.beans.value.ChangeListener;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class ProjectListener {

	private Project project;

	private ChangeListener<String> nameListener;

	public ProjectListener(Project project) {
		this.project = project;

		nameListener = (observable, oldValue, newValue) -> {
			if (ListenerUtils.isNewValueComingFromServer()) {
				return;
			}
			JsonObject json = new JsonObject();
			json.addProperty("id", project.getProjectReference().getUuid().toString());
			json.addProperty("value", project.getProjectReference().getName());

			json.addProperty("field", PropertyDef.PROJECT_NAME);
			json.addProperty("cmd", Commands.PROJECT_UPDATE);

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.push(json);
		};

		project.getProjectReference().nameProperty().addListener(nameListener);
	}

	public void removeListener() {
		project.getProjectReference().nameProperty().addListener(nameListener);
	}
}
