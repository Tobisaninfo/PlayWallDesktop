package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class ReplaceDragMode extends PadDragMode {

	private static final String TYPE = "replace";

	private FontIcon icon;
	private StringProperty displayProperty;

	public ReplaceDragMode() {
		icon = new FontIcon(FontAwesomeType.ARROW_CIRCLE_RIGHT);
		icon.setSize(30);

		displayProperty = new SimpleStringProperty(Localization.getString(Strings.DnDMode_Replace));
	}

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
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
		Pad srcPad = project.getPad(oldPad);

		project.setPad(newPad, srcPad);
		project.setPad(oldPad, new Pad(project, oldPad));
	}

}
