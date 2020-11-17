package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.content.{PadContent, PadContentFactory}
import de.tobias.playpad.pad.preview.PadTextPreview
import de.tobias.playpad.pad.view.IPadContentView
import javafx.scene.layout.Pane

class PlayerPadContentFactory(val `type`: String) extends PadContentFactory(`type`) {

	override def newInstance(pad: Pad): PadContent = new PlayerPadContent(pad, getType)

	override def getPadContentPreview(pad: Pad, parentNode: Pane): IPadContentView = new PadTextPreview(pad, parentNode)

	override def getSupportedTypes: Array[String] = PlayerPadContentFactory.FILE_EXTENSION
}

object PlayerPadContentFactory {
	private val FILE_EXTENSION = Array("*.mp4", "*.mov")
}