package de.tobias.playpad.plugin.content.settings

import de.tobias.playpad.plugin.content.settings.ContentPlayerSettingsViewController.SelectableContentScreen
import javafx.scene.control.ListCell

class ContentScreenCell extends ListCell[SelectableContentScreen] {
	override def updateItem(item: SelectableContentScreen, empty: Boolean): Unit = {
		super.updateItem(item, empty)
		if (!empty) {
			setText(item.getName)
		} else {
			setText("")
		}
	}
}
