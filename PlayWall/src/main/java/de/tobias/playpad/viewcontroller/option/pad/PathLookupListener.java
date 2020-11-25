package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PathLookupListener implements EventHandler<ActionEvent> {

	@Override
	public void handle(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof Node) {
			Object userData = ((Button) source).getUserData();
			if (userData instanceof MediaPath) {
				showPath((MediaPath) userData);
			}
		}
	}

	private void showPath(MediaPath path) {
		NativeApplication.sharedInstance().showFileInFileViewer(path.getPath());
	}
}