package de.tobias.playpad.viewcontroller.option.pad;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;

public class PathLookupListener implements EventHandler<ActionEvent> {

	public void handle(ActionEvent event) {
		System.out.println(event);
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
		System.out.println(path);
		try {
			Desktop.getDesktop().browse(path.getParent().toUri());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
