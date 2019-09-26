package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.midi.Mapping;
import javafx.scene.control.ListCell;

public final class MappingListCell extends ListCell<Mapping> {

	@Override
	protected void updateItem(Mapping item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(item.getName());
		} else {
			setText("");
		}
	}
}