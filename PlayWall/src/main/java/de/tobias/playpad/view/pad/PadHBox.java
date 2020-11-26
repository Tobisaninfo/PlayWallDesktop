package de.tobias.playpad.view.pad;

import de.tobias.playpad.project.page.PadIndex;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;

public class PadHBox extends HBox implements PadIndexable {

	private final ObjectProperty<PadIndex> indexProperty;

	public PadHBox(String... styleClasses) {
		this(0, styleClasses);
	}
	public PadHBox(double spacing, String... styleClasses) {
		super(spacing);
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
