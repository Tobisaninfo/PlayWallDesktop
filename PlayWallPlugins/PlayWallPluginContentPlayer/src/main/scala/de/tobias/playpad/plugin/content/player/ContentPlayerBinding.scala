package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.Zone
import javafx.beans.property.{ReadOnlyDoubleProperty, SimpleDoubleProperty}
import nativecontentplayerwindows.ContentPlayer

class ContentPlayerBinding(val player: ContentPlayer, val zone: Zone) {

	val durationProperty: ReadOnlyDoubleProperty = new SimpleDoubleProperty()
	player.setContentPlayerStopListener((endOfFile)=>{
		if (endOfFile && currentMedia != null) {
			currentMedia.content.pad.setEof(true)
			currentMedia.content.onEof()
		}
	})

	private var currentMedia: ContentPlayerMediaContainer = _

	def play(media: ContentPlayerMediaContainer): Unit = {
		player.Play(media.mediaPath.getPath.toAbsolutePath.toString)
		currentMedia = media
	}

	def resume(media: ContentPlayerMediaContainer): Unit = {
		player.Resume()
		currentMedia = media
	}

	def pause(media: ContentPlayerMediaContainer): Unit = {
		player.Pause()
	}

	def stop(media: ContentPlayerMediaContainer): Unit = {
		player.Stop()
		currentMedia = null
	}

	def highlight(on: Boolean): Unit = {
		// TODO: Implement
	}
}
