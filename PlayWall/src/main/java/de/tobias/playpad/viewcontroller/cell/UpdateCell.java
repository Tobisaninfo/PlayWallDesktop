package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.versionizer.model.Version;
import de.tobias.playpad.Strings;
import javafx.scene.control.ListCell;

public class UpdateCell extends ListCell<Version> {

	@Override
	protected void updateItem(Version item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(Localization.getString(Strings.UI_DIALOG_UPDATE_CELL,
					item.getArtifact().getArtifactId(),
					item.getArtifact().getVersion(),
					item.toVersionString()));
		} else {
			setText("");
		}
	}
}
