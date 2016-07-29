package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.Strings;
import de.tobias.playpad.design.classic.Theme;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ListCell;

public class ThemeCell extends ListCell<Theme> {

	@Override
	protected void updateItem(Theme item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			setText(Localization.getString(Strings.UI_Layout_Classic_Theme_BaseName + item.name()));
		} else {
			setText("");
		}
	}
}
