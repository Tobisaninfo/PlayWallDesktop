package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.Zone
import javafx.application.Platform
import javafx.beans.property.{ObjectProperty, SimpleObjectProperty}
import javafx.util.Duration
import nativecontentplayerwindows.ContentPlayer

class ContentPlayerBinding(val player: ContentPlayer, val zone: Zone) {

	private val positionProperty: ObjectProperty[Duration] = new SimpleObjectProperty[Duration](Duration.ZERO)
	private val durationProperty: ObjectProperty[Duration] = new SimpleObjectProperty[Duration](Duration.ZERO)
	private val currentMedia: ObjectProperty[ContentPlayerMediaContainer] = new SimpleObjectProperty[ContentPlayerMediaContainer]()

	player.setContentPlayerStopListener(endOfFile => {
		if (endOfFile && currentMedia.get() != null) {
			currentMedia.get().content.pad.setEof(true)
			currentMedia.get().content.onEof()
		}
	})
	player.setContentPlayerPositionListener((position, total) => {
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
		player.Play(media.mediaPath.getPath.toAbsolutePath.toString, withFadeIn)
		currentMedia.set(media)
	}

	def resume(media: ContentPlayerMediaContainer, withFadeIn: Boolean): Unit = {
		player.Resume(withFadeIn)
		currentMedia.set(media)
	}

	def pause(media: ContentPlayerMediaContainer): Unit = player.Pause()

	def stop(media: ContentPlayerMediaContainer): Unit = player.Stop()

	def clearHold(): Unit = player.ClearHold()

	def highlight(on: Boolean): Unit = player.HighlightPlayer(on)

	def setFadeValue(value: Double): Unit = player.Fade(value)

	def clear(): Unit = {
		currentMedia.set(null)
	}
}
