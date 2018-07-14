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
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.Optional;
import java.util.UUID;

public class PadSettingsUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Optional<PadSettings> padSettingsOpt = project.getPads().stream().filter(pad -> pad.getPadSettings().getId().equals(uuid)).map(Pad::getPadSettings).findFirst();
				padSettingsOpt.ifPresent(padSettings -> {
					String field = json.get(PropertyDef.FIELD).getAsString();
					switch (field) {
						case PropertyDef.PAD_SETTINGS_LOOP:
							boolean loop = json.get(PropertyDef.VALUE).getAsBoolean();
							Platform.runLater(() -> padSettings.setLoop(loop));
							break;
						case PropertyDef.PAD_SETTINGS_VOLUME:
							double volume = json.get(PropertyDef.VALUE).getAsDouble();
							Platform.runLater(() -> padSettings.setVolume(volume));
							break;
						case PropertyDef.PAD_SETTINGS_TIME_MODE:
							TimeMode timeMode = TimeMode.valueOf(json.get(PropertyDef.VALUE).getAsString());
							Platform.runLater(() -> padSettings.setTimeMode(timeMode));
							break;
						case PropertyDef.PAD_SETTINGS_WARNING:
							Duration warning = Duration.millis(json.get(PropertyDef.VALUE).getAsDouble());
							Platform.runLater(() -> padSettings.setWarning(warning));
							break;
					}
				});
			}
		}
	}
}
