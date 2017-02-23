package de.tobias.playpad.server.sync.listener.downstream.pad;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.listener.downstream.ServerListener;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Created by tobias on 19.02.17.
 */
public class PadMoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();
		Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage()));
	}
}
