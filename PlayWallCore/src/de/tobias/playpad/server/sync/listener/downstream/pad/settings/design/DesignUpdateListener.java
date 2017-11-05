package de.tobias.playpad.server.sync.listener.downstream.pad.settings.design;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernCartDesign2;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.ServerListener;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Handles incoming changes on project from server and set the right property.
 */
public class DesignUpdateListener implements ServerListener {

	@Override
	public void listen(JsonElement element) {
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;

			UUID uuid = UUID.fromString(json.get(PropertyDef.ID).getAsString());
			UUID padId = UUID.fromString(json.get(PropertyDef.DESIGN_PAD_SETTINGS_REF).getAsString());

			Project project = PlayPadPlugin.getImplementation().getCurrentProject();
			if (project != null) {
				Pad pad = project.getPad(padId);
				if (pad != null) {
					ModernCartDesign2 design = pad.getPadSettings().getDesign();
					if (design.getId().equals(uuid)) {

						String field = json.get(PropertyDef.FIELD).getAsString();
						ModernColor color = ModernColor.valueOf(json.get(PropertyDef.VALUE).getAsString());
						IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();

						if (field.equals(PropertyDef.DESIGN_BACKGROUND_COLOR)) {
							Platform.runLater(() -> {
								design.setBackgroundColor(color);
								pad.getPadSettings().setCustomDesign(true);
								mainViewController.loadUserCss();
							});
						} else if (field.equals(PropertyDef.DESIGN_PLAY_COLOR)) {
							Platform.runLater(() -> {
								design.setPlayColor(color);
								pad.getPadSettings().setCustomDesign(true);
								mainViewController.loadUserCss();
							});
						}
					}
				}
			}
		}
	}
}
