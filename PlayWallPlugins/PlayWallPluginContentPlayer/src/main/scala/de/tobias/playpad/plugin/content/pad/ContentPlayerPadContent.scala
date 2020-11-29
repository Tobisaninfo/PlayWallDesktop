package de.tobias.playpad.plugin.content.pad

import java.nio.file.Files
import java.util

import de.tobias.playpad.pad.content.play.{Durationable, Pauseable}
import de.tobias.playpad.pad.content.{PadContent, Playlistable}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.PlayerInstance
import de.tobias.playpad.plugin.content.util._
import de.tobias.playpad.volume.VolumeManager
import javafx.application.Platform
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.util.Duration

import scala.jdk.CollectionConverters._

class ContentPlayerPadContent(val pad: Pad, val `type`: String) extends PadContent(pad) with Pauseable with Durationable with Playlistable {

	private class MediaPlayerContainer(val path: MediaPath, val mediaPlayer: MediaPlayer) {
		def play(): Unit = {
			_durationProperty.bind(mediaPlayer.totalDurationProperty())
			_positionProperty.bind(mediaPlayer.currentTimeProperty())
			ContentPluginMain.playerViewController.showMediaPlayer(getPad.getPadIndex, mediaPlayer, getSelectedZones)

			mediaPlayer.seek(Duration.ZERO)
			mediaPlayer.play()

			currentRunningIndexProperty.set(mediaPlayers.indexOf(this))

			val controller = getPad.getController
			if (controller != null) {
				controller.updatePlaylistLabel()
			}
		}

		def resume(): Unit = {
			mediaPlayer.play()
		}

		def pause(): Unit = {
			mediaPlayer.pause()
		}

		def next(): Unit = {
			stop()

			val index = mediaPlayers.indexOf(this)
			currentRunningIndexProperty.set(index)

			if (index + 1 < mediaPlayers.length) {
				mediaPlayers(index + 1).play()
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

		override def toString: String = f"MediaPlayerContainer: $path"
	}

	private val mediaPlayers: ObservableList[MediaPlayerContainer] = FXCollections.observableArrayList()
	private val currentRunningIndexProperty: IntegerProperty = new SimpleIntegerProperty(-1)

	private val _durationProperty = new SimpleObjectProperty[Duration]
	private val _positionProperty = new SimpleObjectProperty[Duration]

	private var showingLastFrame: Boolean = false
	private var isPause: Boolean = false

	override def getType: String = `type`

	override def currentPlayingMediaIndex: Int = currentRunningIndexProperty.get()

	def currentPlayingMediaIndexProperty(): ReadOnlyIntegerProperty = currentRunningIndexProperty

	override def play(): Unit = {
		if (isPause) {
			mediaPlayers(currentPlayingMediaIndex).resume()
		} else {
			ContentPluginMain.playerViewController.addActivePadToList(getPad.getPadIndex)

			getPad.setEof(false)
			mediaPlayers.head.play()
		}
		showingLastFrame = false
		isPause = false
	}

	override def pause(): Unit = {
		isPause = true
		mediaPlayers(currentPlayingMediaIndex).pause()
	}

	override def next(): Unit = {
		mediaPlayers(currentPlayingMediaIndex).next()
	}

	override def stop(): Boolean = {
		isPause = false
		mediaPlayers(currentPlayingMediaIndex).stop()
		currentRunningIndexProperty.set(-1)

		ContentPluginMain.playerViewController.removeActivePadFromList(getPad.getPadIndex)

		val controller = getPad.getController
		if (controller != null) {
			controller.updatePlaylistLabel()
		}

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
			mediaPlayers(currentPlayingMediaIndex).next()
			return
		}

		ContentPluginMain.playerViewController.removeActivePadFromList(getPad.getPadIndex)
	}

	/*
	Durationable
	 */

	override def getDuration: Duration = _durationProperty.get()

	override def getPosition: Duration = _positionProperty.get()

	override def durationProperty(): ReadOnlyObjectProperty[Duration] = _durationProperty

	override def positionProperty(): ReadOnlyObjectProperty[Duration] = _positionProperty

	override def isPadLoaded: Boolean = {
		mediaPlayers.isNotEmpty
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

			_durationProperty.set(Duration.ZERO)
			_positionProperty.set(Duration.ZERO)

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
			getPad.setEof(true)
			onEof()
		})

		mediaPlayers.add(new MediaPlayerContainer(mediaPath, mediaPlayer))
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

	override def reorderMedia(): Unit = {
		val paths = pad.getPaths
		mediaPlayers.sort((o1, o2) => Integer.compare(paths.indexOf(o1.path), paths.indexOf(o2.path)))
	}

	/*
	 Volume
	 */

	override def updateVolume(): Unit = {
		val volume = VolumeManager.getInstance.computeVolume(getPad)
		mediaPlayers.forEach(player => player.mediaPlayer.setVolume(volume))
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
