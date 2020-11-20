package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.content.{PadContent, PadContentFactory}
import de.tobias.playpad.pad.preview.PadTextPreview
import de.tobias.playpad.pad.view.IPadContentView
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController
import javafx.scene.layout.Pane

class ContentPlayerPadContentFactory(val `type`: String) extends PadContentFactory(`type`) {

	override def newInstance(pad: Pad): PadContent = new ContentPlayerPadContent(pad, getType)

	override def getPadContentPreview(pad: Pad, parentNode: Pane): IPadContentView = new PadTextPreview(pad, parentNode)

	override def getSettingsViewController(pad: Pad): PadSettingsTabViewController = new ContentPlayerPadContentSettingsViewController(pad)

	override def getSupportedTypes: Array[String] = ContentPlayerPadContentFactory.FILE_EXTENSION
}

object ContentPlayerPadContentFactory {
	private val FILE_EXTENSION = Array("*.mp4", "*.mov")

	val lastFrame = "ContentLastFrame"
}