package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.Displayable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class DisplayableCell<T extends Displayable> extends ListCell<T> {

	private Displayable action;

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			if (this.action == null || this.action != item) {
				Node graphics = item.getGraphics();
				setGraphic(graphics);

				textProperty().bind(item.displayProperty());
				this.action = item;
			}
		} else {
			this.action = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");
		}
	}
}
