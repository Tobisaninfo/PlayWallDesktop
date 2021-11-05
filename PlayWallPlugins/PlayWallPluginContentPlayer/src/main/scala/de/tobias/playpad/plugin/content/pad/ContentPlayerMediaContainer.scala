package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.pad.PadStatus
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.util._
import javafx.beans.property.{ReadOnlyObjectProperty, SimpleObjectProperty}
import javafx.util.Duration

class ContentPlayerMediaContainer(val content: ContentPlayerPadContent, val mediaPath: MediaPath) {

	val totalDurationProperty: ReadOnlyObjectProperty[Duration] = new SimpleObjectProperty[Duration]()

	def getTotalDuration: Duration = totalDurationProperty.get()

	def play(): Unit = {
		ContentPluginMain.playerViewController.play(this)

		content.getPad.setEof(false)
		content.currentPlayingMediaIndexProperty().set(content.getMediaPlayers.indexOf(this))
	}

	def resume(): Unit = {
		ContentPluginMain.playerViewController.resume(this)
	}

	def pause(): Unit = {
		ContentPluginMain.playerViewController.pause(this)
	}

	def next(): Unit = {
		stop()

		val players = content.getMediaPlayers
		val index = players.indexOf(this)
		content.currentPlayingMediaIndexProperty().set(index)

		if (index + 1 < players.length) {
			players(index + 1).play()
		} else if (content.getPad.getPadSettings.isLoop) {
			players.head.play()
		} else {
			content.getPad.setStatus(PadStatus.STOP)
		}
	}

	def stop(): Unit = {
		ContentPluginMain.playerViewController.stop(this)

		content._durationProperty.bind(content.totalDurationBinding())
		content._positionProperty.unbind()
		content._positionProperty.set(Duration.ZERO)
	}

	override def toString: String = f"MediaPlayerContainer: $mediaPath"
}