package de.tobias.playpad.view.pad;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.project.page.PadIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;

import static de.tobias.playpad.view.pad.PadStyleClasses.*;

public class PadButton extends Button implements PadIndexable {

	private final ObjectProperty<PadIndex> indexProperty;

	public PadButton(FontIcon icon, EventHandler<ActionEvent> value) {
		super("", icon);

		setFocusTraversable(false);
		setOnAction(value);

		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, STYLE_CLASS_PAD_BUTTON, STYLE_CLASS_PAD_BUTTON_INDEX));
		indexProperty.addListener(new StyleIndexListener(getGraphic(), STYLE_CLASS_PAD_ICON, STYLE_CLASS_PAD_ICON_INDEX));
	}

	public PadIndex getIndex() {
		return indexProperty.get();
	}

	public void setIndex(PadIndex index) {
		indexProperty.set(index);
	}
}
