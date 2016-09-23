package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.PadIndex;
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
	public boolean handle(PadIndex oldPad, PadIndex newPad, Project project) {
		Pad srcPad = project.getPad(oldPad);

		// Alte Pads entfernen, damit keine Nebenabhängigkeiten entstehen in den verschiedenen Seiten
		project.setPad(oldPad, null);
		project.setPad(newPad, null);

		project.setPad(newPad, srcPad);
		return true;
	}

}
