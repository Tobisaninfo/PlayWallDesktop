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
		indexProperty.addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				getStyleClass().removeAll(STYLE_CLASS_PAD_BUTTON, replaceIndex(STYLE_CLASS_PAD_BUTTON_INDEX, oldValue));
				getGraphic().getStyleClass().removeAll(STYLE_CLASS_PAD_ICON, replaceIndex(STYLE_CLASS_PAD_ICON_INDEX, oldValue));
			}

			if (newValue != null) {
				getStyleClass().addAll(STYLE_CLASS_PAD_BUTTON, replaceIndex(STYLE_CLASS_PAD_BUTTON_INDEX, newValue));
				getGraphic().getStyleClass().addAll(STYLE_CLASS_PAD_ICON, replaceIndex(STYLE_CLASS_PAD_ICON_INDEX, newValue));
			}
		});
	}

	public PadIndex getIndex() {
		return indexProperty.get();
	}

	public void setIndex(PadIndex index) {
		indexProperty.set(index);
	}
}
