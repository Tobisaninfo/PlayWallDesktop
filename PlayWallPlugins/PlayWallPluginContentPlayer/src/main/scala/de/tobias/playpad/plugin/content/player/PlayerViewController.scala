package de.tobias.playpad.plugin.content.player

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.ui.NVC
import javafx.geometry.Insets
import javafx.scene.layout._
import javafx.scene.media.{MediaPlayer, MediaView}
import javafx.scene.paint.Color
import javafx.stage.{Stage, StageStyle}

import scala.collection.mutable.ListBuffer

class PlayerViewController extends NVC {

	private class MediaPlayerStack(val x: Double, val y: Double, val width: Double, val height: Double) extends StackPane {
		val mediaPlayer1: MediaView = new MediaView()
		val mediaPlayer2: MediaView = new MediaView()

		getChildren.addAll(mediaPlayer1, mediaPlayer2)

		setLayoutX(x)
		setLayoutY(y)
		setWidth(width)
		setHeight(height)

		def showMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
			if (mediaPlayer1.getMediaPlayer == null) {
				mediaPlayer1.setMediaPlayer(mediaPlayer)
			} else {
				mediaPlayer2.setMediaPlayer(mediaPlayer)
			}
		}
	}

	private val mediaPlayers: ListBuffer[MediaPlayerStack] = ListBuffer.empty

	load("view", "PlayerView")
	applyViewControllerToStage
	Logger.debug("Create Player View Controller")

	override def init(): Unit = {
		mediaPlayers.addOne(new MediaPlayerStack(0, 0, 960, 80))
		mediaPlayers.addOne(new MediaPlayerStack(0, 80, 960, 80))

		val parent = getParent.asInstanceOf[Pane]
		mediaPlayers.foreach(player => parent.getChildren.add(player))

		parent.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)))
	}

	override def initStage(stage: Stage): Unit = {
		super.initStage(stage)
		stage.initStyle(StageStyle.UNDECORATED)
		stage.setAlwaysOnTop(true)

		stage.getScene.setFill(Color.BLACK)

		stage.setX(0)
		stage.setY(0)
		stage.setWidth(960)
		stage.setHeight(160)
	}

	def showMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
		mediaPlayers.foreach(view => view.showMediaPlayer(mediaPlayer))
	}
}
