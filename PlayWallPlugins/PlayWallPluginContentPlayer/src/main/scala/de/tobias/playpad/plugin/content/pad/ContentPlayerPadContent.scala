package de.tobias.playpad.plugin.content.pad

import java.nio.file.Files
import java.util
import java.util.stream.Collectors

import de.tobias.playpad.pad.content.play.{Durationable, Pauseable}
import de.tobias.playpad.pad.content.{PadContent, Playlistable}
import de.tobias.playpad.pad.fade.{Fadeable, LinearFadeController}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.{Zone, ZoneConfiguration}
import de.tobias.playpad.plugin.content.util._
import de.tobias.playpad.profile.Profile
import de.tobias.playpad.volume.VolumeManager
import javafx.application.Platform
import javafx.beans.binding.{Bindings, ObjectBinding}
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.media.{Media, MediaPlayer}
import javafx.util.Duration

import scala.jdk.CollectionConverters._

class ContentPlayerPadContent(val pad: Pad, val `type`: String) extends PadContent(pad) with Pauseable with Durationable with Playlistable with Fadeable {

	private val mediaPlayers: ObservableList[ContentPlayerMediaContainer] = FXCollections.observableArrayList()
	private val currentRunningIndexProperty: IntegerProperty = new SimpleIntegerProperty(-1)

	private[pad] val _durationProperty = new SimpleObjectProperty[Duration]
	private[pad] val _positionProperty = new SimpleObjectProperty[Duration]

	private var showingLastFrame: Boolean = false
	private var isPause: Boolean = false

	private val fadeController = new LinearFadeController(value => {
		if (currentPlayingMediaIndex >= 0) {
			ContentPluginMain.playerViewController
				.setFadeValue(mediaPlayers(currentPlayingMediaIndex).mediaPlayer, getSelectedZones, value)
		}
	})

	override def getType: String = `type`

	override def currentPlayingMediaIndex: Int = currentRunningIndexProperty.get()

	def currentPlayingMediaIndexProperty(): IntegerProperty = currentRunningIndexProperty

	def getMediaPlayers: ObservableList[ContentPlayerMediaContainer] = mediaPlayers

	/*
	Control Methods
	 */

	override def play(): Unit = {
		if (isPause) {
			mediaPlayers(currentPlayingMediaIndex).resume()
		} else {
			ContentPluginMain.playerViewController.addActivePadToList(getPad.getPadIndex, getSelectedZones)

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

		ContentPluginMain.playerViewController.removeActivePadFromList(getPad.getPadIndex, getSelectedZones)

		val controller = getPad.getController
		if (controller != null) {
			controller.updatePlaylistLabel()
		}

		true
	}

	def onEof(): Unit = {
		if (isFadeActive) {
			ContentPluginMain.playerViewController.removeActivePadFromList(getPad.getPadIndex, getSelectedZones)
			return
		}

		if (shouldShowLastFrame() && !showingLastFrame // Only is settings is enabled and not already in last frame state
			&& !pad.getPadSettings.isLoop // Only go to last frame state, is looping is disabled
		) {
			getPad.setStatus(PadStatus.PAUSE)
			showingLastFrame = true
			return
		}

		showingLastFrame = false

		if (getPad.isEof) {
			mediaPlayers(currentPlayingMediaIndex).next()
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

	def totalDurationBinding(): ObjectBinding[Duration] = {
		Bindings.createObjectBinding(() => mediaPlayers.stream()
			.map(player => player.mediaPlayer.getTotalDuration)
			.filter(duration => duration != null)
			.reduce(Duration.ZERO, (o1: Duration, o2: Duration) => o1.add(o2)),
			mediaPlayers.stream().map(player => {
				if (player.mediaPlayer != null) {
					player.mediaPlayer.totalDurationProperty()
				} else {
					null
				}
			})
				.filter(o => o != null)
				.toArray(size => new Array[ReadOnlyObjectProperty[Duration]](size)): _*)
	}

	/*
	Fadeable
	 */

	override def fadeIn(): Unit = {
		val fadeIn = getPad.getPadSettings.getFade.getFadeIn
		if (fadeIn.toMillis > 0) {
			fadeController.fadeIn(fadeIn)
		}
	}

	override def fadeOut(onFinish: Runnable): Unit = {
		val fadeOut = getPad.getPadSettings.getFade.getFadeOut
		if (fadeOut.toMillis > 0) {
			fadeController.fadeOut(fadeOut, () => {
				if (onFinish != null) onFinish.run()
				updateVolume()
			})
		}
		else {
			onFinish.run()
		}
	}

	override def isFadeActive: Boolean = fadeController.isFading

	override def fade(from: Double, to: Double, duration: Duration, onFinish: Runnable): Unit = {
		fadeController.fade(from, to, duration, onFinish)
	}

	/*
	Loading
	 */

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

			_durationProperty.bind(totalDurationBinding())
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

		mediaPlayers.add(new ContentPlayerMediaContainer(this, mediaPath, mediaPlayer))
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

		if (index >= 0) {
			val playerContainer = mediaPlayers(index)
			playerContainer.stop()
			mediaPlayers.remove(index)
		}
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

	/*
	Custom Settings
	 */

	def shouldShowLastFrame(): Boolean = {
		pad.getPadSettings.getCustomSettings.getOrDefault(ContentPlayerPadContentFactory.lastFrame, false).asInstanceOf[Boolean]
	}

	def getSelectedZones: Seq[Zone] = {
		val zoneConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ZoneConfiguration]

		val customSettings = pad.getPadSettings.getCustomSettings
		val selectedZoneNames = customSettings.getOrDefault(
			ContentPlayerPadContentFactory.zones,
			zoneConfiguration.zones.stream().map(zone => zone.getName).collect(Collectors.toList())
		).asInstanceOf[util.List[String]]
		zoneConfiguration.zones.asScala.filter(zone => selectedZoneNames.contains(zone.getName)).toSeq
	}
}
