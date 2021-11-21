package de.tobias.playpad.plugin.api.cell;

import de.tobias.playpad.plugin.api.settings.WebApiRemoteSettings;
import javafx.scene.control.ListCell;

public class WebApiRemoteCell extends ListCell<WebApiRemoteSettings> {

	@Override
	protected void updateItem(WebApiRemoteSettings item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(item.getName());
		} else {
			setText("");
		}
	}
}
