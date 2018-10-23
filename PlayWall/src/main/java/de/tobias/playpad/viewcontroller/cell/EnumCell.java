package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.util.Localization;
import javafx.scene.control.ListCell;

public class EnumCell<T extends Enum<?>> extends ListCell<T> {

	private String baseName;

	public EnumCell(String baseName) {
		this.baseName = baseName;
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(Localization.getString(baseName + item.name()));
		} else {
			setText("");
		}
	}
}
