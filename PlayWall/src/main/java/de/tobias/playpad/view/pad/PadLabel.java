package de.tobias.playpad.view.pad;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.project.page.PadIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

import static de.tobias.playpad.view.pad.PadStyleClasses.*;

public class PadLabel extends Label implements PadIndexable {

	private final ObjectProperty<PadIndex> indexProperty;

	public PadLabel(FontIcon icon, String... styleClasses) {
		this("", styleClasses);
		setGraphic(icon);
	}

	public PadLabel(String text, String... styleClasses) {
		super(text);

		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener((observable, oldValue, newValue) -> {
			if (oldValue != null) {
				for (String styleClass : styleClasses) {
					getStyleClass().remove(PadStyleClasses.replaceIndex(styleClass, oldValue));
				}
				if (getGraphic() != null) {
					getGraphic().getStyleClass().removeAll(STYLE_CLASS_PAD_ICON, replaceIndex(STYLE_CLASS_PAD_ICON_INDEX, oldValue));
				}
			}

			if (newValue != null) {
				for (String styleClass : styleClasses) {
					getStyleClass().add(PadStyleClasses.replaceIndex(styleClass, newValue));
				}
				if (getGraphic() != null) {
					getGraphic().getStyleClass().addAll(STYLE_CLASS_PAD_ICON, replaceIndex(STYLE_CLASS_PAD_ICON_INDEX, newValue));
				}
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
