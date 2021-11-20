package de.tobias.playpad.plugin.content.pad

import de.thecodelabs.logger.Logger
import de.tobias.playpad.pad.content.play.{Durationable, Pauseable}
import de.tobias.playpad.pad.content.{PadContent, Playlistable}
import de.tobias.playpad.pad.fade.{Fadeable, LinearFadeController}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.{Zone, ContentPlayerPluginConfiguration}
import de.tobias.playpad.plugin.content.util._
import de.tobias.playpad.profile.Profile
import javafx.application.Platform
import javafx.beans.binding.{Bindings, ObjectBinding}
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.util.Duration
import nativecontentplayerwindows.ContentPlayer

import java.nio.file.Files
import java.util
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._

class ContentPlayerPadContent(val pad: Pad, val `type`: String) extends PadContent(pad) with Pauseable with Durationable with Playlistable with Fadeable {

	private val mediaPlayers: ObservableList[ContentPlayerMediaContainer] = FXCollections.observableArrayList()
	private val currentRunningIndexProperty: IntegerProperty = new SimpleIntegerProperty(-1)

	private[content] val _durationProperty = new SimpleObjectProperty[Duration]
	private[content] val _positionProperty = new SimpleObjectProperty[Duration]

	private var showingLastFrame: Boolean = false
	private var isPause: Boolean = false

	private val fadeController = new LinearFadeController(value => {
		if (getCurrentPlayingMediaIndex >= 0) {
			ContentPluginMain.playerViewController.setFadeValue(getSelectedZones, value)
		}
	})

	override def getType: String = `type`

	override def getCurrentPlayingMediaIndex: Int = currentRunningIndexProperty.get()

	override def currentPlayingMediaIndexProperty(): IntegerProperty = currentRunningIndexProperty

	def getMediaPlayers: ObservableList[ContentPlayerMediaContainer] = mediaPlayers

	override def hasNext: Boolean = getCurrentPlayingMediaIndex + 1 < mediaPlayers.length

	/*
	Control Methods
	 */

	override def play(withFadeIn: Boolean): Unit = {
		if (isPause) {
			mediaPlayers(getCurrentPlayingMediaIndex).resume(withFadeIn)
		} else {
			getPad.setEof(false)
			mediaPlayers.head.play(withFadeIn)
		}
		showingLastFrame = false
		isPause = false
	}

	override def pause(): Unit = {
		isPause = true
		mediaPlayers(getCurrentPlayingMediaIndex).pause()
	}

	override def next(): Unit = {
		mediaPlayers(getCurrentPlayingMediaIndex).next()
	}

	override def stop(): Boolean = {
		isPause = false
		mediaPlayers(getCurrentPlayingMediaIndex).stop()

		if (showingLastFrame) {
			ContentPluginMain.playerViewController.clearHold(mediaPlayers(getCurrentPlayingMediaIndex))
		}

		currentRunningIndexProperty.set(-1)
		true
	}

	def onEof(): Unit = {
		// By default the last frame will be displayed. Only under certain conditions the last frame will be cleared
		// 1. User settings set to "Clear last frame"
		// 2. There is no loop
		// 3. There is no playlist
		if (!pad.getPadSettings.isLoop && getCurrentPlayingMediaIndex + 1 == mediaPlayers.length) {
			if (!shouldShowLastFrame()) {
				Logger.debug(s"Clear last frame for pad ${pad.getPadIndex}")
				ContentPluginMain.playerViewController.clearHold(mediaPlayers(getCurrentPlayingMediaIndex))
			} else {
				showingLastFrame = true
				return
			}
		}

		showingLastFrame = false

		if (getPad.isEof) {
			mediaPlayers(getCurrentPlayingMediaIndex).next()
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
		Bindings.createObjectBinding(() => {
			val durations: util.List[Duration] = mediaPlayers.stream()
			  .map(player => player.getTotalDuration)
			  .filter(duration => duration != null)
			  .collect(Collectors.toList())
			val totalDuration: Duration = durations
			  .stream()
			  .reduce(Duration.ZERO, (o1: Duration, o2: Duration) => o1.add(o2))
			totalDuration
		}, mediaPlayers.stream().map(player => player.totalDurationProperty).toArray(size => new Array[ReadOnlyObjectProperty[Duration]](size)): _*)
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

				if (getPad.getStatus == PadStatus.PLAY) {
					getPad.setStatus(PadStatus.STOP)
				}
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

	override def isLoaded(mediaPath: MediaPath): Boolean = {
		true
	}


	override def isPadLoading: Boolean = false

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

		val duration = Duration.seconds(ContentPlayer.GetTotalDuration(path.toAbsolutePath.toString))
		mediaPlayers.add(new ContentPlayerMediaContainer(this, mediaPath, duration))

		Platform.runLater(() => {
			getPad.setStatus(PadStatus.READY)

			_durationProperty.bind(totalDurationBinding())
			_positionProperty.set(Duration.ZERO)

			if (getPad.isPadVisible) {
				getPad.getController.getView.showBusyView(false)
			}
		})
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
		// not needed
	}

	override def reorderMedia(): Unit = {
		val paths = pad.getPaths
		mediaPlayers.sort((o1, o2) => Integer.compare(paths.indexOf(o1.mediaPath), paths.indexOf(o2.mediaPath)))
	}

	/*
	 Volume
	 */

	override def updateVolume(): Unit = {
		// not needed
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
		val zoneConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ContentPlayerPluginConfiguration]

		val customSettings = pad.getPadSettings.getCustomSettings
		val selectedZoneNames = customSettings.getOrDefault(
			ContentPlayerPadContentFactory.zones,
			zoneConfiguration.zones.stream().map(zone => zone.getName).collect(Collectors.toList())
		).asInstanceOf[util.List[String]]
		zoneConfiguration.zones.asScala.filter(zone => selectedZoneNames.contains(zone.getName)).toSeq
	}
}
