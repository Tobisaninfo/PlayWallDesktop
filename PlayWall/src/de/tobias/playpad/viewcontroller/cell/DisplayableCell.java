package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.Displayable;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public class DisplayableCell<T extends Displayable> extends ListCell<T> {

	private Displayable action;

	@Override
	protected void updateItem(T action, boolean empty) {
		super.updateItem(action, empty);
		if (!empty) {
			if (this.action == null || this.action != action) {
				Node graphics = action.getGraphics();
				setGraphic(graphics);

				textProperty().bind(action.displayProperty());
				this.action = action;
			}
		} else {
			this.action = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");
		}
	}
}
