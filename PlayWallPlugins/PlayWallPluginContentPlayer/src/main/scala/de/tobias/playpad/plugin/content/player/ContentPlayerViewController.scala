package de.tobias.playpad.plugin.content.player

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.ui.NVC
import de.thecodelabs.utils.ui.size.IgnoreStageSizing
import de.tobias.playpad.plugin.content.settings.{PlayerInstance, PlayerInstanceConfiguration}
import de.tobias.playpad.project.page.PadIndex
import javafx.geometry.Insets
import javafx.scene.layout._
import javafx.scene.media.MediaPlayer
import javafx.scene.paint.Color
import javafx.stage.{Stage, StageStyle}

import scala.collection.mutable.ListBuffer

@IgnoreStageSizing
class ContentPlayerViewController extends NVC {

	private val mediaStacks: ListBuffer[MediaPlayerStack] = ListBuffer.empty

	load("view", "PlayerView")
	applyViewControllerToStage
	Logger.debug("Create Player View Controller")

	override def init(): Unit = {
		val parent = getParent.asInstanceOf[Pane]
		parent.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)))
	}

	override def initStage(stage: Stage): Unit = {
		super.initStage(stage)
		stage.initStyle(StageStyle.UNDECORATED)
		stage.setAlwaysOnTop(true)

		stage.getScene.setFill(Color.BLACK)
	}

	def showMediaPlayer(padIndex: PadIndex, mediaPlayer: MediaPlayer, zones: Seq[PlayerInstance]): Unit = {
		val iterator = this.mediaStacks.iterator
		while (iterator.hasNext) {
			val mediaPlayerStack = iterator.next()
			if (zones.contains(mediaPlayerStack.playerInstance)) {
				mediaPlayerStack.showMediaPlayer(padIndex, mediaPlayer)
			}
		}
	}

	def disconnectMediaPlayer(mediaPlayer: MediaPlayer, zones: Seq[PlayerInstance]): Unit = {
		val iterator = this.mediaStacks.iterator
		while (iterator.hasNext) {
			val mediaPlayerStack = iterator.next()
			if (zones.contains(mediaPlayerStack.playerInstance)) {
				mediaPlayerStack.disconnectMediaPlayer(mediaPlayer)
			}
		}
	}

	def configurePlayers(configuration: PlayerInstanceConfiguration): Unit = {
		val parent = getParent.asInstanceOf[Pane]
		parent.getChildren.clear()

		mediaStacks.clear()
		configuration.instances.forEach(player => {
			val mediaPlayerStack = new MediaPlayerStack(player)
			mediaStacks.addOne(mediaPlayerStack)
			parent.getChildren.add(mediaPlayerStack)
		})

		getStageContainer.ifPresent(container => {
			val stage = container.getStage

			import scala.jdk.CollectionConverters._
			val instances = configuration.instances.asScala
			val maxWidth = instances.map(player => player.x + player.width).max
			val maxHeight = instances.map(player => player.y + player.height).max

			stage.setX(0)
			stage.setY(0)
			stage.setWidth(maxWidth)
			stage.setHeight(maxHeight)
		})
	}

	def addActivePadToList(padIndex: PadIndex, zones: Seq[PlayerInstance]): Unit = getMediaStacks(zones)
		.foreach(mediaStack => mediaStack.addActivePad(padIndex))

	def removeActivePadFromList(padIndex: PadIndex, zones: Seq[PlayerInstance]): Unit = getMediaStacks(zones)
		.foreach(mediaStack => mediaStack.removeActivePad(padIndex))

	def highlight(zone: PlayerInstance, on: Boolean): Unit = {
		getMediaStack(zone).head.highlight(on)
	}

	def setFadeValue(mediaPlayer: MediaPlayer, zones: Seq[PlayerInstance], value: Double): Unit = getMediaStacks(zones)
		.foreach(mediaStack => mediaStack.setFadeValue(mediaPlayer, value))

	private def getMediaStack(zone: PlayerInstance): ListBuffer[MediaPlayerStack] = {
		getMediaStacks(List(zone))
	}

	private def getMediaStacks(zones: Seq[PlayerInstance]): ListBuffer[MediaPlayerStack] = {
		mediaStacks.filter(mediaPlayer => zones.contains(mediaPlayer.playerInstance))
	}
}
