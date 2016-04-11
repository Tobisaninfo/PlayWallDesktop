package de.tobias.playpad.pad.drag;

import de.tobias.playpad.project.Project;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class ReplaceDragMode extends PadDragMode {

	private static final String TYPE = "replace";

	private FontIcon icon;

	public ReplaceDragMode() {
		icon = new FontIcon(FontAwesomeType.ARROW_CIRCLE_RIGHT);
		icon.setSize(30);
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty("Ersetzen"); // TODO Localize
	}

	@Override
	public Node getGraphics() {
		return icon;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void handle(int oldPad, int newPad, Project project) {
		project.replacePads(oldPad, newPad);
	}

}
