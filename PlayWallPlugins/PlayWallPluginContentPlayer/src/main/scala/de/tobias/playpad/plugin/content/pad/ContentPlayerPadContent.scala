package de.tobias.playpad.plugin.content.pad

import de.tobias.playpad.pad.content.play.{Durationable, Pauseable}
import de.tobias.playpad.pad.content.{PadContent, PlaylistListener, Playlistable}
import de.tobias.playpad.pad.fade.{Fadeable, LinearFadeController}
import de.tobias.playpad.pad.mediapath.MediaPath
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.{ContentPlayerPluginConfiguration, Zone}
import de.tobias.playpad.plugin.content.util._
import de.tobias.playpad.profile.Profile
import de.tobias.playpad.tigger.{LocalPadTrigger, TriggerPoint}
import javafx.application.Platform
import javafx.beans.binding.{Bindings, ObjectBinding}
import javafx.beans.property._
import javafx.collections.{FXCollections, ObservableList}
import javafx.util.Duration
import nativecontentplayerwindows.ContentPlayer

import java.nio.file.Files
import java.util
import java.util.stream.Collectors
import java.util.{Collections, UUID}
import scala.jdk.CollectionConverters._

class ContentPlayerPadContent(val pad: Pad, val `type`: String) extends PadContent(pad) with Pauseable with Durationable with Playlistable with Fadeable {

	private val mediaPlayers: ObservableList[ContentPlayerMediaContainer] = FXCollections.observableArrayList()
	private val currentRunningIndexProperty: IntegerProperty = new SimpleIntegerProperty(-1)

	private[content] val _durationProperty = new SimpleObjectProperty[Duration]
	private[content] val _positionProperty = new SimpleObjectProperty[Duration]

	private[content] val listeners: util.Set[PlaylistListener] = new util.HashSet[PlaylistListener]()

	private var showingLastFrame: Boolean = false
	private var isPause: Boolean = false

	var stopMediaByOtherPlayer = false

	private val fadeController = new LinearFadeController(value => {
		if (getCurrentPlayingMediaIndex >= 0) {
			ContentPluginMain.playerViewController.setFadeValue(getSelectedZones, value)
		}
	})

	override def getType: String = `type`

	override def getCurrentPlayingMediaIndex: Int = currentRunningIndexProperty.get()

	override def currentPlayingMediaIndexProperty(): IntegerProperty = currentRunningIndexProperty

	def getMediaContainers: ObservableList[ContentPlayerMediaContainer] = mediaPlayers

	override def hasNext: Boolean = getCurrentPlayingMediaIndex + 1 < mediaPlayers.length

	/*
	Control Methods
	 */

	override def play(withFadeIn: Boolean): Unit = {
		if (isPause) {
			mediaPlayers(getCurrentPlayingMediaIndex).resume(withFadeIn)
		} else {
			if (isShuffle) {
				Collections.shuffle(mediaPlayers)
			} else {
				reorderMedia()
			}
			getPad.setEof(false)
			mediaPlayers.head.play(withFadeIn)
		}
		showingLastFrame = false
		isPause = false
		stopMediaByOtherPlayer = false
	}

	override def pause(): Unit = {
		isPause = true
		mediaPlayers(getCurrentPlayingMediaIndex).pause()
	}

	override def next(): Unit = {
		mediaPlayers(getCurrentPlayingMediaIndex).next()
	}

	override def stop(): Boolean = {
		if (isPause) {
			play(false)
		}
		if (getCurrentPlayingMediaIndex != -1) {
			mediaPlayers(getCurrentPlayingMediaIndex).stop()

			if (showingLastFrame && !stopMediaByOtherPlayer) {
				ContentPluginMain.playerViewController.clearHold(mediaPlayers(getCurrentPlayingMediaIndex))
			}
		}

		currentRunningIndexProperty.set(-1)
		true
	}

	def onEof(): Unit = {
		val hasLocalPadTrigger = getPad.getPadSettings.getTrigger(TriggerPoint.EOF)
		  .getItems
		  .stream()
		  .filter(item => item.isInstanceOf[LocalPadTrigger])
		  .map(item => item.asInstanceOf[LocalPadTrigger])
		  .filter(item => hasPadTriggerInterferingZones(item))
		  .count() > 0

		val noFurtherItemsInPlaylist = getCurrentPlayingMediaIndex + 1 == mediaPlayers.length

		// By default the last frame will be displayed. Only under certain conditions the last frame will be cleared
		// 1. User settings set to "Clear last frame"
		// 2. There is no loop
		// 3. There is no playlist
		if (!pad.getPadSettings.isLoop && noFurtherItemsInPlaylist) {
			if (shouldShowLastFrame() || hasLocalPadTrigger) {
				showingLastFrame = true
				return
			} else {
				ContentPluginMain.playerViewController.clearHold(mediaPlayers(getCurrentPlayingMediaIndex))
			}
		}

		showingLastFrame = false
		// Only automatically go to the next playlist item, if auto next is active or
		// the item is the last one (next() go into stop state if no item is left)
		if (isAutoNext || noFurtherItemsInPlaylist) {
			mediaPlayers(getCurrentPlayingMediaIndex).next()
		}
	}

	private def hasPadTriggerInterferingZones(item: LocalPadTrigger): Boolean = {
		item.getCarts.stream().anyMatch(id => {
			val content = pad.getProject.getPad(id).getContent
			if (!content.isInstanceOf[ContentPlayerPadContent]) {
				return false
			}
			val targetZones = content.asInstanceOf[ContentPlayerPadContent].getSelectedZones
			return targetZones.exists(zone => getSelectedZones.contains(zone))
		})
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
		mediaPlayers.removeIf(player => player.mediaPath == mediaPath)
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

	def shouldShowLastFrame(): Boolean = pad.getPadSettings.getCustomSettings.getOrDefault(ContentPlayerPadContentFactory.lastFrame, false).asInstanceOf[Boolean]

	def isShuffle: Boolean = pad.getPadSettings.getCustomSettings.getOrDefault(Playlistable.SHUFFLE_SETTINGS_KEY, false).asInstanceOf[Boolean]

	def isAutoNext: Boolean = pad.getPadSettings.getCustomSettings.getOrDefault(Playlistable.AUTO_NEXT_SETTINGS_KEY, false).asInstanceOf[Boolean]

	def getSelectedZones: Seq[Zone] = {
		val zoneConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ContentPlayerPluginConfiguration]

		val customSettings = pad.getPadSettings.getCustomSettings
		val selectedZoneIds = customSettings.getOrDefault(
			ContentPlayerPadContentFactory.zones,
			zoneConfiguration.zones.stream().map(zone => zone.id).collect(Collectors.toList())
		).asInstanceOf[util.List[UUID]]
		zoneConfiguration.zones.asScala.filter(zone => selectedZoneIds.contains(zone.id)).toSeq
	}

	/*
	Listener
	 */

	override def addPlaylistListener(listener: PlaylistListener): Unit = listeners.add(listener)

	override def removePlaylistListener(listener: PlaylistListener): Unit = listeners.remove(listener)
}
