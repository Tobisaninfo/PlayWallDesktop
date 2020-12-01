package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.pad.PadStatus
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.util._
import javafx.scene.media.MediaPlayer
import javafx.util.Duration

class ContentPlayerMediaContainer(val content: ContentPlayerPadContent, val path: MediaPath, val mediaPlayer: MediaPlayer) {
	def play(): Unit = {
		content._durationProperty.bind(mediaPlayer.totalDurationProperty())
		content._positionProperty.bind(mediaPlayer.currentTimeProperty())
		ContentPluginMain.playerViewController.showMediaPlayer(content.getPad.getPadIndex, mediaPlayer, content.getSelectedZones)

		mediaPlayer.seek(Duration.ZERO)
		mediaPlayer.play()

		content.getPad.setEof(false)
		content.currentPlayingMediaIndexProperty().set(content.getMediaPlayers.indexOf(this))
	}

	def resume(): Unit = {
		mediaPlayer.play()
	}

	def pause(): Unit = {
		mediaPlayer.pause()
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
		mediaPlayer.stop()
		ContentPluginMain.playerViewController.disconnectMediaPlayer(mediaPlayer, content.getSelectedZones)

		content._durationProperty.bind(content.totalDurationBinding())
		content._positionProperty.unbind()
		content._positionProperty.set(Duration.ZERO)
	}

	override def toString: String = f"MediaPlayerContainer: $path"
}