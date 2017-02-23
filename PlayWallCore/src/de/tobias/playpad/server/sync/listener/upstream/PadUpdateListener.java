package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import javafx.beans.value.ChangeListener;

import java.util.function.Consumer;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class PadUpdateListener {

	private Pad pad;

	private ChangeListener<String> nameListener;
	private ChangeListener<Number> positionListener;
	private ChangeListener<String> contentTypeListener;

	public PadUpdateListener(Pad pad) {
		this.pad = pad;

		nameListener = (observable, oldValue, newValue) -> {
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PAD_NAME);
				json.addProperty(PropertyDef.VALUE, newValue);
			});
		};

		positionListener = (observable, oldValue, newValue) -> {
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PAD_POSITION);
				json.addProperty(PropertyDef.VALUE, newValue);
			});
		};

		contentTypeListener = (observable, oldValue, newValue) -> {
			handleInvalidation(json -> {
				json.addProperty(PropertyDef.FIELD, PropertyDef.PAD_CONTENT_TYPE);
				json.addProperty(PropertyDef.VALUE, newValue);
			});
		};
	}

	private void handleInvalidation(Consumer<JsonObject> handler) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.ID, pad.getUuid().toString());
		json.addProperty(PropertyDef.PAD_PAGE, pad.getPage().getId().toString());
		json.addProperty(PropertyDef.CMD, Commands.PAD_UPDATE);

		handler.accept(json);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			pad.nameProperty().addListener(nameListener);
			pad.positionProperty().addListener(positionListener);
			pad.contentTypeProperty().addListener(contentTypeListener);
		}
	}

	public void removeListener() {
		added = false;
		pad.nameProperty().addListener(nameListener);
		pad.positionProperty().removeListener(positionListener);
		pad.contentTypeProperty().removeListener(contentTypeListener);
	}
}
