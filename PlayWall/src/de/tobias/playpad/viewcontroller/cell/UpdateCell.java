package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.Strings;
import de.tobias.playpad.update.PlayPadUpdater;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class UpdateCell extends ListCell<Updatable> {

	@Override
	protected void updateItem(Updatable item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			if (item instanceof PlayPadUpdater) {
				setGraphic(new ImageView("de/tobias/playpad/assets/files/class_obj.png"));
			} else {
				setGraphic(new ImageView("de/tobias/playpad/assets/files/enum_obj.png"));
			}
			setText(Localization.getString(Strings.UI_Dialog_Update_Cell, item.name(), item.getCurrentVersion(), item.getNewVersion()));
		} else {
			setText("");
		}
	}
}
