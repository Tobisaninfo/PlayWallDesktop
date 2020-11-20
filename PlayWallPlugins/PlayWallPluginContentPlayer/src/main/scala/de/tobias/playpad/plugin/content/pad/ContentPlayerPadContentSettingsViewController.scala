package de.tobias.playpad.plugin.content.pad

import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController
import javafx.fxml.FXML
import javafx.scene.control.CheckBox

class ContentPlayerPadContentSettingsViewController(val pad: Pad) extends PadSettingsTabViewController {

	@FXML
	var lastFrameCheckbox: CheckBox = _

	load("view", "ContentPadSettings", Localization.getBundle)

	override def getName: String = Localization.getString("plugin.content.player.settings")

	override def loadSettings(pad: Pad): Unit = {
		val customSettings = pad.getPadSettings.getCustomSettings
		val lastFrameOption = customSettings.get(ContentPlayerPadContentFactory.lastFrame)
		if (lastFrameOption != null) {
			lastFrameCheckbox.setSelected(lastFrameOption.toString.toBoolean)
		}
	}

	override def saveSettings(pad: Pad): Unit = {
		val customSettings = pad.getPadSettings.getCustomSettings
		customSettings.put(ContentPlayerPadContentFactory.lastFrame, lastFrameCheckbox.isSelected)
	}
}
