package de.tobias.playpad.plugin.content.pad

import java.nio.file.Files
import java.util

import de.tobias.playpad.pad.content.play.{Durationable, Pauseable}
import de.tobias.playpad.pad.content.{PadContent, PlaylistAppendable}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.PlayerInstance
import de.tobias.playpad.volume.VolumeManager
import javafx.application.Platform
import javafx.beans.property.{ReadOnlyObjectProperty, SimpleObjectProperty}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.util.Duration

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

class ContentPlayerPadContent(val pad: Pad, val `type`: String) extends PadContent(pad) with Pauseable with Durationable with PlaylistAppendable {

	private class MediaPlayerContainer(val path: MediaPath, val mediaPlayer: MediaPlayer) {
		def play(): Unit = {
			_durationProperty.bind(mediaPlayer.totalDurationProperty())
			_positionProperty.bind(mediaPlayer.currentTimeProperty())

			mediaPlayer.seek(Duration.ZERO)

			ContentPluginMain.playerViewController.showMediaPlayer(mediaPlayer, getSelectedZones)

			mediaPlayer.play()
			currentRunningIndex = mediaPlayers.indexOf(this)
		}

		def resume(): Unit = {
			mediaPlayer.play()
		}

		def pause(): Unit = {
			mediaPlayer.pause()
		}

		def next(): Unit = {
			stop()

			currentRunningIndex = mediaPlayers.indexOf(this)
			if (currentRunningIndex + 1 < mediaPlayers.length) {
				mediaPlayers(currentRunningIndex + 1).play()
			} else if (getPad.getPadSettings.isLoop) {
				mediaPlayers.head.play()
			} else {
				getPad.setStatus(PadStatus.STOP)
			}
		}

		def stop(): Unit = {
			mediaPlayer.stop()
			ContentPluginMain.playerViewController.disconnectMediaPlayer(mediaPlayer, getSelectedZones)

			_durationProperty.unbind()
			_durationProperty.set(Duration.ZERO)
			_positionProperty.unbind()
			_positionProperty.set(Duration.ZERO)
		}
	}

	private val mediaPlayers: ListBuffer[MediaPlayerContainer] = ListBuffer.empty
	private var currentRunningIndex: Int = 0

	private val _durationProperty = new SimpleObjectProperty[Duration]
	private val _positionProperty = new SimpleObjectProperty[Duration]

	private var showingLastFrame: Boolean = false
	private var isPause: Boolean = false

	override def getType: String = `type`

	override def play(): Unit = {
		if (isPause) {
			mediaPlayers(currentRunningIndex).resume()
		} else {
			getPad.setEof(false)
			mediaPlayers.head.play()
		}
		showingLastFrame = false
		isPause = false
	}

	override def pause(): Unit = {
		isPause = true
		mediaPlayers(currentRunningIndex).pause()
	}

	override def stop(): Boolean = {
		isPause = false
		mediaPlayers(currentRunningIndex).stop()
		true
	}

	def onEof(): Unit = {
		if (shouldShowLastFrame() && !showingLastFrame && !pad.getPadSettings.isLoop) {
			getPad.setStatus(PadStatus.PAUSE)
			showingLastFrame = true
			return
		}

		showingLastFrame = false

		if (getPad.isEof) {
			mediaPlayers(currentRunningIndex).next()
			return
		}
	}

	/*
	Durationable
	 */

	override def getDuration: Duration = _durationProperty.get()

	override def getPosition: Duration = _positionProperty.get()

	override def durationProperty(): ReadOnlyObjectProperty[Duration] = _durationProperty

	override def positionProperty(): ReadOnlyObjectProperty[Duration] = _positionProperty

	override def isPadLoaded: Boolean = {
		mediaPlayers.nonEmpty
	}

	/**
	 * Load media files.
	 */
	override def loadMedia(): Unit = {
		mediaPlayers.clear()
		getPad.getPaths.forEach(loadMedia(_))
	}

	/**
	 * Load media file.
	 *
	 * @param mediaPath specify media path
	 */
	override def loadMedia(mediaPath: MediaPath): Unit = {
		val path = mediaPath.getPath
		if (Files.notExists(path)) {
			Platform.runLater(() => getPad.setStatus(PadStatus.NOT_FOUND))
			return
		}

		val media = new Media(path.toUri.toString)
		val mediaPlayer = new MediaPlayer(media)

		mediaPlayer.setOnReady(() => {
			getPad.setStatus(PadStatus.READY)

			Platform.runLater(() => {
				if (getPad.isPadVisible) {
					getPad.getController.getView.showBusyView(false)
				}
			})
		})

		mediaPlayer.setOnError(() => Platform.runLater(() => {
			if (getPad.isPadVisible) {
				getPad.getController.getView.showBusyView(false)
			}
		}))

		mediaPlayer.setOnEndOfMedia(() => {
			if (getPad.getPadSettings.isLoop) {
				mediaPlayer.seek(Duration.ZERO)
				mediaPlayer.play()
			} else { // Loop
				getPad.setEof(true)
				onEof()
			}
		})

		mediaPlayers.addOne(new MediaPlayerContainer(mediaPath, mediaPlayer))
	}

	/**
	 * Unload media files.
	 */
	override def unloadMedia(): Unit = {
		if ((getPad.getStatus eq PadStatus.PLAY) || (getPad.getStatus eq PadStatus.PAUSE)) getPad.setStatus(PadStatus.STOP)

		mediaPlayers.clear()

		Platform.runLater(() => {
			if (getPad != null) {
				getPad.setStatus(PadStatus.EMPTY)
			}
		})
	}

	/**
	 * Unload media file.
	 *
	 * @param mediaPath specify media path
	 */
	override def unloadMedia(mediaPath: MediaPath): Unit = {
		val index = mediaPlayers.indexWhere(item => item.path.getId == mediaPath.getId)

		val playerContainer = mediaPlayers(index)
		playerContainer.stop()

		mediaPlayers.remove(index)
	}

	override def updateVolume(): Unit = {
		val volume = VolumeManager.getInstance.computeVolume(getPad)
		mediaPlayers.foreach(player => player.mediaPlayer.setVolume(volume))
	}

	/**
	 * Create a copy of the PadContent instance
	 *
	 * @param pad target pad
	 * @return copied content
	 */
	override def copy(pad: Pad): PadContent = {
		val clone = new ContentPlayerPadContent(pad, getType)
		clone.loadMedia()
		clone
	}

	def shouldShowLastFrame(): Boolean = {
		pad.getPadSettings.getCustomSettings.getOrDefault(ContentPlayerPadContentFactory.lastFrame, false).asInstanceOf[Boolean]
	}

	def getSelectedZones: Seq[PlayerInstance] = {
		val customSettings = pad.getPadSettings.getCustomSettings
		val selectedZoneNames = customSettings.getOrDefault(ContentPlayerPadContentFactory.zones, new util.ArrayList[String]()).asInstanceOf[util.List[PlayerInstance]]
		ContentPluginMain.configuration.instances.asScala.filter(zone => selectedZoneNames.contains(zone.getName)).toSeq
	}
}
