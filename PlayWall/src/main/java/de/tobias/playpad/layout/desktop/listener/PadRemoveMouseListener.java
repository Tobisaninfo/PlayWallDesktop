package de.tobias.playpad.layout.desktop.listener;

import de.tobias.playpad.pad.Pad;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

/**
 * Created by tobias on 18.03.17.
 */
public class PadRemoveMouseListener implements EventHandler<MouseEvent> {
	@Override
	public void handle(MouseEvent event) {
		if (event.getSource() instanceof StackPane) {
			StackPane view = (StackPane) event.getSource();
			if (view.getUserData() instanceof Pad) {
				Pad pad = (Pad) view.getUserData();
				if (event.getButton() == MouseButton.SECONDARY) {
					pad.clear();
				}
			}
		}
	}
}
