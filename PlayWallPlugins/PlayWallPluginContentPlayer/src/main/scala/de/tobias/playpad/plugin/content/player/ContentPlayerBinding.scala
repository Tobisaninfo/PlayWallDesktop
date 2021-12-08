package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.Zone
import javafx.application.Platform
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.util.Duration
import nativecontentplayerwindows.ContentPlayer

class ContentPlayerBinding(val nativePlayer: ContentPlayer, val zone: Zone) {

	private val positionProperty: ObjectProperty[Duration] = new SimpleObjectProperty[Duration](Duration.ZERO)
	private val durationProperty: ObjectProperty[Duration] = new SimpleObjectProperty[Duration](Duration.ZERO)
	private val currentMedia: ObjectProperty[ContentPlayerMediaContainer] = new SimpleObjectProperty[ContentPlayerMediaContainer]()

	nativePlayer.setContentPlayerStopListener(endOfFile => {
		if (endOfFile && currentMedia.get() != null) {
			currentMedia.get().content.pad.setEof(true)
			currentMedia.get().content.onEof()
		}
	})
	nativePlayer.setContentPlayerPositionListener((position, total) => {
		Platform.runLater(() => {
			val totalDuration = Duration.seconds(total)
			if (totalDuration != durationProperty.get()) {
				durationProperty.setValue(totalDuration)
			}
			positionProperty.setValue(Duration.seconds(position))
		})
	})
	currentMedia.addListener((_, oldValue, newValue) => {
		if (oldValue != null) {
			oldValue.content._positionProperty.unbind()
			oldValue.content._durationProperty.unbind()
		}
		if (newValue != null) {
			newValue.content._positionProperty.bind(positionProperty)
			newValue.content._durationProperty.bind(durationProperty)
		}
	})

	def play(media: ContentPlayerMediaContainer, withFadeIn: Boolean): Unit = {
		if (currentMedia.get() != null && currentMedia.get().content.getPad != media.content.getPad) {
			if (currentMedia.get().content.getPad.isPlay) {
				// Stop the current playing media on this player and hold the last frame
				currentMedia.get().content.stopMediaByOtherPlayer = true
				currentMedia.get().content.getPad.stop()
			} else if (currentMedia.get().content.getPad.isPaused) {
				// The player must be resumed before playing the next media
				nativePlayer.Resume(withFadeIn)
				currentMedia.get().content.getPad.stop()
			}
		}
		nativePlayer.Play(media.getPath.toString, withFadeIn)
		currentMedia.set(media)
	}

	def resume(media: ContentPlayerMediaContainer, withFadeIn: Boolean): Unit = {
		nativePlayer.Resume(withFadeIn)
		currentMedia.set(media)
	}

	def pause(media: ContentPlayerMediaContainer): Unit = nativePlayer.Pause()

	def stop(media: ContentPlayerMediaContainer): Unit = {
		// If media is stopped by a different pad, the current media should keep playing to have a smooth transition
		// to the new media. Otherwise the media will be stopped normally.
		if (!media.content.stopMediaByOtherPlayer) {
			nativePlayer.Stop()
			currentMedia.set(null)
		}
	}

	def clearHold(): Unit = nativePlayer.ClearHold()

	def highlight(on: Boolean): Unit = nativePlayer.HighlightPlayer(on)

	def setFadeValue(value: Double): Unit = nativePlayer.Fade(value)

	def clear(): Unit = {
		currentMedia.set(null)
	}
}
