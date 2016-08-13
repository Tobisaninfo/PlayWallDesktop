package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class MoveDragMode extends PadDragMode {

	private static final String TYPE = "move";

	private FontIcon icon;
	private StringProperty displayProperty;

	public MoveDragMode() {
		icon = new FontIcon(FontAwesomeType.ARROWS);
		icon.setSize(30);

		displayProperty = new SimpleStringProperty(Localization.getString(Strings.DnDMode_Move));
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
	public void handle(PadIndex oldIndex, PadIndex newIndex, ProjectV2 project) {
		Pad oldPad = project.getPad(oldIndex);
		Pad newPad = project.getPad(newIndex);

		project.setPad(newIndex, oldPad);
		project.setPad(oldIndex, newPad);
	}

}
