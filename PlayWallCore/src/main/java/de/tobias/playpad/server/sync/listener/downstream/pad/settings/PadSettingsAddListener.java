package de.tobias.playpad.server.sync.listener.downstream.pad.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import javafx.util.Duration;

import java.util.UUID;

public class PadSettingsAddListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID padId = UUID.fromString(json.get(PropertyDef.PAD_SETTINGS_PAD_ID).getAsString());
			boolean loop = json.get(PropertyDef.PAD_SETTINGS_LOOP).getAsBoolean();
			double volume = json.get(PropertyDef.PAD_SETTINGS_VOLUME).getAsDouble();
			TimeMode timeMode = null;
			if (!json.get(PropertyDef.PAD_SETTINGS_TIME_MODE).isJsonNull()) {
				timeMode = TimeMode.valueOf(json.get(PropertyDef.PAD_SETTINGS_TIME_MODE).getAsString());
			}
			Duration warning = null;
			if (!json.get(PropertyDef.PAD_SETTINGS_WARNING).isJsonNull()) {
				warning = Duration.millis(json.get(PropertyDef.PAD_SETTINGS_WARNING).getAsDouble());
			}

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(padId);
				if (pad != null) {
					PadSettings settings = pad.getPadSettings();
					settings.setId(uuid);
					settings.setLoop(loop);
					settings.setVolume(volume);
					settings.setTimeMode(timeMode);
					settings.setWarning(warning);
				}
			}
		}
	}
}
