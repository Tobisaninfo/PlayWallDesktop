package de.tobias.playpad;

import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public interface Displayable {

	StringProperty displayProperty();

	default Node getGraphics() {
		return null;
	}
}
