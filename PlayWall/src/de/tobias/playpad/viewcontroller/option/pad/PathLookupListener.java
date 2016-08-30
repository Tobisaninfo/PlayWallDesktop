package de.tobias.playpad.viewcontroller.option.pad;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.utils.ui.Alertable;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

public class PathLookupListener implements EventHandler<ActionEvent> {

	private Alertable alertable;

	public PathLookupListener(Alertable alertable) {
		this.alertable = alertable;
	}

	public void handle(ActionEvent event) {
		Object source = event.getSource();
		if (source instanceof Button) {
			// single path
			Object userData = ((Button) source).getUserData();
			if (userData instanceof Path) {
				showPath((Path) userData);
			}
		} else if (source instanceof MenuItem) {
			// multiple path
			Object userData = ((MenuItem) source).getUserData();
			if (userData instanceof Path) {
				showPath((Path) userData);
			}
		}
	}

	private void showPath(Path path) {
		try {
			Desktop.getDesktop().browse(path.getParent().toUri());
		} catch (IOException e) {
			String string = Localization.getString(Strings.Error_Standard_Gen, e.getMessage());
			alertable.showErrorMessage(string, PlayPadPlugin.getImplementation().getIcon());
			e.printStackTrace();
		}
	}
}
