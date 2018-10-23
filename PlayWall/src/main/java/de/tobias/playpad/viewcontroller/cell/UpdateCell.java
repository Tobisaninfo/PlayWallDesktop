package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.update.PlayPadUpdater;
import de.tobias.updater.client.Updatable;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class UpdateCell extends ListCell<Updatable> {

	@Override
	protected void updateItem(Updatable item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			if (item instanceof PlayPadUpdater) {
				setGraphic(new ImageView("gfx/class_obj.png"));
			} else {
				setGraphic(new ImageView("gfx/enum_obj.png"));
			}
			setText(Localization.getString(Strings.UI_Dialog_Update_Cell, item.name(), item.getCurrentVersion(), item.getNewVersion()));
		} else {
			setText("");
		}
	}
}
