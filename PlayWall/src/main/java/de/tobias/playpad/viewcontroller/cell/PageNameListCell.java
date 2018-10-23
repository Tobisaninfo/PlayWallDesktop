package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.page.Page;
import javafx.scene.control.ListCell;

public final class PageNameListCell extends ListCell<Integer> {

	@Override
	protected void updateItem(Integer item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			Page page = PlayPadMain.getProgramInstance().getCurrentProject().getPage(item);
			String name = page.getName();
			if (name.isEmpty()) {
				name = Localization.getString(Strings.UI_Window_Main_PageButton, (item));
			}
			setText(name);
		} else {
			setText("");
		}
	}
}