package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.pad.Pad
import de.tobias.playpad.pad.content.{PadContent, PadContentFactory, PadContentPlaylistFactory}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.view.IPadContentView
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController
import javafx.scene.Node
import javafx.scene.layout.Pane

class ContentPlayerPadContentFactory(val `type`: String) extends PadContentFactory(`type`) with PadContentPlaylistFactory {

	override def newInstance(pad: Pad): PadContent = new ContentPlayerPadContent(pad, getType)

	override def getPadContentPreview(pad: Pad, parentNode: Pane): IPadContentView = new ContentPlayerPadPreview(pad, parentNode)

	override def getSettingsViewController(pad: Pad): PadSettingsTabViewController = new ContentPlayerPadContentSettingsViewController(pad)

	override def getSupportedTypes: Array[String] = ContentPlayerPadContentFactory.FILE_EXTENSION

	override def getCustomPlaylistItemView(pad: Pad, mediaPath: MediaPath): Node = new ContentPlayerPlaylistView(pad, mediaPath)
}

object ContentPlayerPadContentFactory {
	private val FILE_EXTENSION = Array("*.mp4", "*.mkv", "*.mov")

	val lastFrame = "ContentLastFrame"
	val zones = "zones"
}