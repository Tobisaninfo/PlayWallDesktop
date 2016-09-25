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

public class DuplicateDragMode extends PadDragMode {

	private static final String TYPE = "duplicate";

	private FontIcon icon;
	private StringProperty displayProperty;

	public DuplicateDragMode() {
		icon = new FontIcon(FontAwesomeType.COPY);
		icon.setSize(30);

		displayProperty = new SimpleStringProperty(Localization.getString(Strings.DnDMode_Duplicate));
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
	public boolean handle(PadIndex oldIndex, PadIndex newIndex, Project project) {
		Pad oldPad = project.getPad(oldIndex);
		try {
			Pad copyPad = oldPad.clone();

			// Alte Pads entfernen, damit keine Nebenabhängigkeiten entstehen in den verschiedenen Seiten
			project.setPad(newIndex, null);

			// Neue Pads in die Seiten einfügen
			project.setPad(newIndex, copyPad);
			return true;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return false;
	}
}