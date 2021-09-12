package de.tobias.playpad.view.pad;

import de.tobias.playpad.project.page.PadIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;

public class PadStackPane extends StackPane implements PadIndexable {

	private final ObjectProperty<PadIndex> indexProperty;

	public PadStackPane(String... styleClasses) {
		indexProperty = new SimpleObjectProperty<>();
		indexProperty.addListener(new StyleIndexListener(this, styleClasses));
	}

	public PadIndex getIndex() {
		return indexProperty.get();
	}

	public void setIndex(PadIndex index) {
		indexProperty.set(index);
	}
}
