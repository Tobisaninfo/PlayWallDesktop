package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import javafx.beans.value.ChangeListener;

import java.util.function.Consumer;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class PageUpdateListener {

	private Page page;

	private ChangeListener<String> nameListener;
	private ChangeListener<Number> positionListener;

	public PageUpdateListener(Page page) {
		this.page = page;

		nameListener = (observable, oldValue, newValue) -> {
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PAGE_NAME);
				json.addProperty(PropertyDef.VALUE, newValue);
			});
		};

		positionListener = (observable, oldValue, newValue) -> {
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PAGE_POSITION);
				json.addProperty(PropertyDef.VALUE, newValue);
			});
		};
	}

	private void handleInvalidation(Consumer<JsonObject> handler) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.ID, page.getId().toString());
		json.addProperty(PropertyDef.PAGE_PROJECT, page.getProject().getProjectReference().getUuid().toString());
		json.addProperty(PropertyDef.CMD, Commands.PAGE_UPDATE);

		handler.accept(json);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

	public void addListener() {
		page.nameProperty().addListener(nameListener);
		page.positionProperty().addListener(positionListener);
	}

	public void removeListener() {
		page.nameProperty().addListener(nameListener);
		page.positionProperty().removeListener(positionListener);
	}
}
