package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.pad.PadStatus
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.util._
import javafx.beans.property.{ObjectProperty, ReadOnlyObjectProperty, SimpleObjectProperty}
import javafx.util.Duration

import java.nio.file.{Files, Path}

class ContentPlayerMediaContainer(val content: ContentPlayerPadContent, private[pad] val mediaPath: MediaPath, val totalDuration: Duration) {

	private val _totalDurationProperty: ObjectProperty[Duration] = new SimpleObjectProperty[Duration]()

	_totalDurationProperty.set(totalDuration)

	def getPath: Path = {
		val sourcePath = mediaPath.getPath.toAbsolutePath

		val globalSettings = PlayPadPlugin.getInstance.getGlobalSettings
		val convertPath = globalSettings.getCachePath.resolve(sourcePath.getFileName + ".mp4")

		if (Files.exists(convertPath)) {
			return convertPath
		}

		sourcePath
	}

	def getTotalDuration: Duration = _totalDurationProperty.get()

	def totalDurationProperty: ReadOnlyObjectProperty[Duration] = _totalDurationProperty

	def play(withFadeIn: Boolean): Unit = {
		ContentPluginMain.playerViewController.play(this, withFadeIn)

		content.getPad.setEof(false)
		content.currentPlayingMediaIndexProperty().set(content.getMediaContainers.indexOf(this))
	}

	def resume(withFadeIn: Boolean): Unit = {
		ContentPluginMain.playerViewController.resume(this, withFadeIn)
	}

	def pause(): Unit = {
		ContentPluginMain.playerViewController.pause(this)
	}

	def next(): Unit = {
		val players = content.getMediaContainers
		val currentIndex = players.indexOf(this)
		content.currentPlayingMediaIndexProperty().set(currentIndex)


		if (currentIndex + 1 < players.length) {
			content.listeners.forEach(listener => listener.onNextItem(content.pad, currentIndex + 1, players.length))
			players(currentIndex + 1).play(false)
		} else if (content.getPad.getPadSettings.isLoop) {
			content.listeners.forEach(listener => listener.onNextItem(content.pad, 0, players.length))
			players.head.play(false)
		} else {
			content.getPad.setStatus(PadStatus.STOP)
		}
	}

	def stop(): Unit = {
		ContentPluginMain.playerViewController.stop(this)

		if (!content.getPad.getPadSettings.isLoop) {
			content._durationProperty.bind(content.totalDurationBinding())
			content._positionProperty.unbind()
			content._positionProperty.set(Duration.ZERO)
		}
	}

	override def toString: String = f"MediaPlayerContainer: $mediaPath"
}