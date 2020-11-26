package de.tobias.playpad.view.pad;

import de.tobias.playpad.project.page.PadIndex;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public class StyleIndexListener implements ChangeListener<PadIndex> {

	private final Node node;
	private final String[] styleClasses;

	public StyleIndexListener(Node node, String... styleClasses) {
		this.node = node;
		this.styleClasses = styleClasses;
	}

	@Override
	public void changed(ObservableValue<? extends PadIndex> observable, PadIndex oldValue, PadIndex newValue) {
		if (oldValue != null) {
			for (String styleClass : styleClasses) {
				node.getStyleClass().remove(PadStyleClasses.replaceIndex(styleClass, oldValue));
			}
		}

		if (newValue != null) {
			for (String styleClass : styleClasses) {
				node.getStyleClass().add(PadStyleClasses.replaceIndex(styleClass, newValue));
			}
		}
	}
}
