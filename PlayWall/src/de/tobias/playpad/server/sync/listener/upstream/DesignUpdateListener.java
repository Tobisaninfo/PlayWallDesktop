package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.value.ChangeListener;

import java.util.function.Consumer;

/**
 * Created by tobias on 24.02.17.
 */
public class DesignUpdateListener {

	private ModernCartDesign design;

	private ChangeListener<ModernColor> backgroundColorListener;
	private ChangeListener<ModernColor> playColorListener;

	public DesignUpdateListener(ModernCartDesign design) {
		this.design = design;

		backgroundColorListener = (observable, oldValue, newValue) -> handleInvalidation(json -> {
			json.addProperty(PropertyDef.FIELD, PropertyDef.DESIGN_BACKGROUND_COLOR);
			json.addProperty(PropertyDef.VALUE, newValue.name());
		});

		playColorListener = (observable, oldValue, newValue) -> handleInvalidation(json -> {
			json.addProperty(PropertyDef.FIELD, PropertyDef.DESIGN_PLAY_COLOR);
			json.addProperty(PropertyDef.VALUE, newValue.name());
		});
	}

	private void handleInvalidation(Consumer<JsonObject> handler) {
		if (ServerUtils.isNewValueComingFromServer()) {
			return;
		}
		JsonObject json = new JsonObject();
		json.addProperty(PropertyDef.ID, design.getId().toString());
		json.addProperty(PropertyDef.DESIGN_PAD_REF, design.getPad().getUuid().toString());
		json.addProperty(PropertyDef.CMD, Commands.DESIGN_UPDATE);

		handler.accept(json);

		Server server = PlayPadPlugin.getServerHandler().getServer();
		server.push(json);
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			design.backgroundColorProperty().addListener(backgroundColorListener);
			design.playColorProperty().addListener(playColorListener);
		}
	}

	public void removeListener() {
		added = false;
		design.backgroundColorProperty().removeListener(backgroundColorListener);
		design.playColorProperty().removeListener(playColorListener);
	}
}
