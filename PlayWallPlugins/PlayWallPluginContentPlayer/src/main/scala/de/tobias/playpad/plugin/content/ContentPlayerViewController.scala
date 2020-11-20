package de.tobias.playpad.plugin.content

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.ui.NVC
import de.thecodelabs.utils.ui.size.IgnoreStageSizing
import de.tobias.playpad.plugin.content.settings.PlayerInstanceConfiguration
import javafx.geometry.Insets
import javafx.scene.layout._
import javafx.scene.media.{MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.{Stage, StageStyle}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

@IgnoreStageSizing
class ContentPlayerViewController extends NVC {

	private class MediaPlayerStack(val x: Double, val y: Double, val width: Double, val height: Double) extends StackPane {

		val mediaViews: mutable.Map[MediaPlayer, MediaView] = new mutable.HashMap[MediaPlayer, MediaView]()

		setLayoutX(x)
		setLayoutY(y)
		setWidth(width)
		setHeight(height)

		def showMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
			if (!mediaViews.contains(mediaPlayer)) {
				mediaViews.put(mediaPlayer, new MediaView(mediaPlayer))
			}

			val mediaView = mediaViews(mediaPlayer)

			getChildren.add(mediaView)
		}

		def disconnectMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
			getChildren.remove(mediaViews(mediaPlayer))
		}
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

	def showMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
		val iterator = this.mediaPlayers.iterator
		while (iterator.hasNext) {
			val mediaPlayerStack = iterator.next()
			mediaPlayerStack.showMediaPlayer(mediaPlayer)
		}
	}

	def disconnectMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
		val iterator = this.mediaPlayers.iterator
		while (iterator.hasNext) {
			val mediaPlayerStack = iterator.next()
			mediaPlayerStack.disconnectMediaPlayer(mediaPlayer)
		}
	}

	def configurePlayers(configuration: PlayerInstanceConfiguration): Unit = {
		mediaPlayers.clear()
		configuration.instances.forEach(player => {
			mediaPlayers.addOne(new MediaPlayerStack(player.x, player.y, player.width, player.height))
		})

		val parent = getParent.asInstanceOf[Pane]
		parent.getChildren.clear()
		mediaPlayers.foreach(player => parent.getChildren.add(player))

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
}
