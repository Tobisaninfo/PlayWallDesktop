package de.tobias.playpad.plugin.content.player

import java.util.stream.Collectors

import de.thecodelabs.logger.Logger
import de.tobias.playpad.plugin.content.settings.Zone
import de.tobias.playpad.project.page.PadIndex
import javafx.geometry.Insets
import javafx.scene.layout.{Background, BackgroundFill, CornerRadii, StackPane}
import javafx.scene.media.{MediaPlayer, MediaView}
import javafx.scene.paint.Color

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MediaPlayerStack(val zone: Zone) extends StackPane {

	private var activePlayers: ListBuffer[PadIndex] = ListBuffer.empty
	val mediaViews: mutable.Map[MediaPlayer, MediaView] = new mutable.HashMap[MediaPlayer, MediaView]()

	setLayoutX(zone.x)
	setLayoutY(zone.y)
	setMinWidth(zone.width)
	setMaxWidth(zone.width)
	setMinHeight(zone.height)
	setMaxHeight(zone.height)

	def addActivePad(padIndex: PadIndex): Unit = activePlayers.addOne(padIndex)

	def removeActivePad(padIndex: PadIndex): Unit = activePlayers = activePlayers.filter(element => element != padIndex)

	def showMediaPlayer(padIndex: PadIndex, mediaPlayer: MediaPlayer): Unit = {
		if (!mediaViews.contains(mediaPlayer)) {
			val mediaView = new MediaView(mediaPlayer)
			mediaView.setFitWidth(zone.width)
			mediaView.setFitHeight(zone.height)
			mediaViews.put(mediaPlayer, mediaView)
		}

		val mediaView = mediaViews(mediaPlayer)
		mediaView.setUserData(padIndex)
		mediaView.setOpacity(1.0)

		if (!getChildren.contains(mediaView)) {
			val index = activePlayers.indexOf(padIndex)
			try {
				getChildren.add(index, mediaView)
			} catch {
				case e: Exception =>
					Logger.error(e)
					getChildren.add(mediaView)
			}
		}
	}

	def disconnectMediaPlayer(mediaPlayer: MediaPlayer): Unit = {
		if (mediaViews.contains(mediaPlayer)) {
			getChildren.remove(mediaViews(mediaPlayer))
		}
	}

	def setFadeValue(mediaPlayer: MediaPlayer, value: Double): Unit = {
		if (mediaViews.contains(mediaPlayer)) {
			val mediaView = mediaViews(mediaPlayer)
			mediaView.setOpacity(value)
		}
	}

	def highlight(on: Boolean): Unit = {
		if (on) {
			setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)))
		} else {
			setBackground(null)
		}
	}

	override def toString: String = f"MediaPlayerStack: ${getChildren.stream().map(view => f"MediaView: ${view.getUserData}").collect(Collectors.joining(", "))}"
}
