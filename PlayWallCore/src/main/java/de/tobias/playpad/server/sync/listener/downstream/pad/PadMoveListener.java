package de.tobias.playpad.server.sync.listener.downstream.pad;

import com.google.gson.JsonElement;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.server.sync.listener.ServerListener;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

/**
 * Created by tobias on 19.02.17.
 */
public class PadMoveListener implements ServerListener {
	@Override
	public void listen(JsonElement element) {
		if (PlayPadPlugin.getInstance().getCurrentProject() != null) {
			IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
			Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage()));
		}
	}
}
