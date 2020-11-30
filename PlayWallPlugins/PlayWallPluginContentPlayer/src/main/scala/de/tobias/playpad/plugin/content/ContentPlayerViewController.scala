package de.tobias.playpad.plugin.content

import java.util.stream.Collectors

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.ui.NVC
import de.thecodelabs.utils.ui.size.IgnoreStageSizing
import de.tobias.playpad.plugin.content.settings.{PlayerInstance, PlayerInstanceConfiguration}
import de.tobias.playpad.project.page.PadIndex
import javafx.geometry.Insets
import javafx.scene.layout._
import javafx.scene.media.{MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.{Stage, StageStyle}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@IgnoreStageSizing
class ContentPlayerViewController extends NVC {

	private class MediaPlayerStack(val playerInstance: PlayerInstance) extends StackPane {

		private var activePlayers: ListBuffer[PadIndex] = ListBuffer.empty
		val mediaViews: mutable.Map[MediaPlayer, MediaView] = new mutable.HashMap[MediaPlayer, MediaView]()

		setLayoutX(playerInstance.x)
		setLayoutY(playerInstance.y)
		setWidth(playerInstance.width)
		setHeight(playerInstance.height)

		def addActivePad(padIndex: PadIndex): Unit = activePlayers.addOne(padIndex)

		def removeActivePad(padIndex: PadIndex): Unit = activePlayers = activePlayers.filter(element => element != padIndex)

		def showMediaPlayer(padIndex: PadIndex, mediaPlayer: MediaPlayer): Unit = {
			if (!mediaViews.contains(mediaPlayer)) {
				val mediaView = new MediaView(mediaPlayer)
				mediaView.setFitWidth(playerInstance.width)
				mediaView.setFitHeight(playerInstance.height)
				mediaViews.put(mediaPlayer, mediaView)
			}

			val mediaView = mediaViews(mediaPlayer)
			mediaView.setUserData(padIndex)

			if (!getChildren.contains(mediaView)) {
				val index = activePlayers.indexOf(padIndex)
				getChildren.add(index, mediaView)
			}
		}

		def disconnectMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
			if (mediaViews.contains(mediaPlayer)) {
				getChildren.remove(mediaViews(mediaPlayer))
			}
		}

		override def toString: String = f"MediaPlayerStack: ${getChildren.stream().map(view => f"MediaView: ${view.getUserData}").collect(Collectors.joining(", "))}"
	}

	private val mediaPlayers: ListBuffer[MediaPlayerStack] = ListBuffer.empty

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
		val iterator = this.mediaPlayers.iterator
		while (iterator.hasNext) {
			val mediaPlayerStack = iterator.next()
			if (zones.contains(mediaPlayerStack.playerInstance)) {
				mediaPlayerStack.showMediaPlayer(padIndex, mediaPlayer)
			}
		}
	}

	def disconnectMediaPlayer(mediaPlayer: MediaPlayer, zones: Seq[PlayerInstance]): Unit = {
		val iterator = this.mediaPlayers.iterator
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

		mediaPlayers.clear()
		configuration.instances.forEach(player => {
			val mediaPlayerStack = new MediaPlayerStack(player)
			mediaPlayers.addOne(mediaPlayerStack)
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

	def addActivePadToList(padIndex: PadIndex, zones: Seq[PlayerInstance]): Unit = mediaPlayers
		.filter(mediaPlayer => zones.contains(mediaPlayer.playerInstance))
		.foreach(mediaPlayer => mediaPlayer.addActivePad(padIndex))

	def removeActivePadFromList(padIndex: PadIndex, zones: Seq[PlayerInstance]): Unit = mediaPlayers
		.filter(mediaPlayer => zones.contains(mediaPlayer.playerInstance))
		.foreach(mediaPlayer => mediaPlayer.removeActivePad(padIndex))
}
