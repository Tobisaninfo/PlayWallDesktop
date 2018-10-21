package de.tobias.playpad.server.sync.listener.downstream.pad.settings.design;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;

import java.util.UUID;

/**
 * Created by tobias on 24.02.17.
 */
public class DesignAddListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID padId = UUID.fromString(json.get(PropertyDef.DESIGN_PAD_SETTINGS_REF).getAsString());
			ModernColor backgroundColor = ModernColor.valueOf(json.get(PropertyDef.DESIGN_BACKGROUND_COLOR).getAsString());
			ModernColor playColor = ModernColor.valueOf(json.get(PropertyDef.DESIGN_PLAY_COLOR).getAsString());

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(padId);
				if (pad != null) {
					ModernCartDesign modernCartDesign = new ModernCartDesign(pad, uuid);
					modernCartDesign.setBackgroundColor(backgroundColor);
					modernCartDesign.setPlayColor(playColor);
					pad.getPadSettings().setDesign(modernCartDesign);
				}
			}
		}
	}
}
