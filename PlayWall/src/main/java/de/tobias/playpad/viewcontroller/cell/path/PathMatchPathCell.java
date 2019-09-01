package de.tobias.playpad.viewcontroller.cell.path;

import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.dialog.PathMatchDialog;
import javafx.scene.control.TableCell;

public class PathMatchPathCell extends TableCell<PathMatchDialog.TempMediaPath, PathMatchDialog.TempMediaPath> {

	@Override
	protected void updateItem(PathMatchDialog.TempMediaPath item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			pseudoClassStateChanged(PseudoClasses.DEACTIVATED_CLASS, !item.isMatched());
			if (item.isMatched()) {
				setText(item.getLocalPath().toString());
			} else {
				setText(item.getMediaPath().getPath().toString());
			}
		} else {
			setText(null);
		}
	}
}
