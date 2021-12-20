package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.util.Localization;
import javafx.scene.control.ListCell;

public class LocalizeCell extends ListCell<String> {

	private final String baseName;

	public LocalizeCell(String baseName) {
		this.baseName = baseName;
	}

	@Override
	protected void updateItem(String item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(Localization.getString(baseName + item));
		} else {
			setText("");
		}
	}
}
