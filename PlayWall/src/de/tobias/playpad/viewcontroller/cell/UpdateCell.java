package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.Strings;
import de.tobias.playpad.Updatable;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ListCell;

public class UpdateCell extends ListCell<Updatable> {

	@Override
	protected void updateItem(Updatable item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(Localization.getString(Strings.UI_Dialog_Update_Cell, item.name(), item.getCurrentVersion(), item.getNewVersion()));
		} else {
			setText("");
		}
	}
}
