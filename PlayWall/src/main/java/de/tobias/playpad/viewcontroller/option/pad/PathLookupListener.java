package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.utils.application.system.NativeApplication;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

import java.nio.file.Path;

public class PathLookupListener implements EventHandler<ActionEvent> {

	@Override
	public void handle(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof Button) {
			// single path
			Object userData = ((Button) source).getUserData();
			if (userData instanceof MediaPath) {
				showPath((MediaPath) userData);
			}
		} else if (source instanceof MenuItem) {
			// multiple path
			Object userData = ((MenuItem) source).getUserData();
			if (userData instanceof Path) {
				showPath((MediaPath) userData);
			}
		}
	}

	private void showPath(MediaPath path) {
		NativeApplication.sharedInstance().showFileInFileViewer(path.getPath());
	}
}