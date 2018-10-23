package de.tobias.playpad.view;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class EmptyPadView extends Label {

	public EmptyPadView(Pane parent) {
		super(Localization.getString(Strings.Content_Empty));
		setWrapText(true);
		setAlignment(Pos.CENTER);
		setTextAlignment(TextAlignment.CENTER);
		prefWidthProperty().bind(parent.widthProperty());
		setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(this, Priority.ALWAYS);
	}
}
