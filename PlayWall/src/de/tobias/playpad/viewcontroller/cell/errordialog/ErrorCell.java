package de.tobias.playpad.viewcontroller.cell.errordialog;

import de.tobias.playpad.pad.PadException;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;

public class ErrorCell extends TableCell<PadException, String> {

	public ErrorCell() {
		Text text = new Text();
		text.getStyleClass().add("label");
		setGraphic(text);
		setPrefHeight(Control.USE_COMPUTED_SIZE);
		text.wrappingWidthProperty().bind(widthProperty());
		text.textProperty().bind(itemProperty());
	}
}
