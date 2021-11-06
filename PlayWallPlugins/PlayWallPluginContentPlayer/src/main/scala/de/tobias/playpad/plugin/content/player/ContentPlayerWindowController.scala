package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.{Zone, ZoneConfiguration}
import nativecontentplayerwindows.{ContentPlayer, ContentPlayerWindow}

import scala.collection.mutable.ListBuffer

class ContentPlayerWindowController {

	var window: ContentPlayerWindow = _
	val players: ListBuffer[ContentPlayerBinding] = ListBuffer.empty

	def configurePlayers(configuration: ZoneConfiguration): Unit = {
		players.foreach(_.clear())
		players.clear()
		if (window != null) {
			window.Close()
		}

		window = new ContentPlayerWindow();
		window.SetIcon(PlayPadPlugin.getInstance.getIconData)
		window.Show()

		configuration.zones.forEach(zone => {
			val contentPlayer = new ContentPlayer(zone.toNative)
			window.AddContentPlayer(contentPlayer)

			players.addOne(new ContentPlayerBinding(contentPlayer, zone))
		})

		import scala.jdk.CollectionConverters._
		val zones = configuration.zones.asScala
		val maxWidth = zones.map(player => player.x + player.width).max.toInt
		val maxHeight = zones.map(player => player.y + player.height).max.toInt

		window.SetLocation(0, 0)
		window.SetSize(maxWidth, maxHeight)
	}

	private def getContentPlayerBinding(zone: Zone): ContentPlayerBinding = {
		val zones = getContentPlayerBindings(List(zone))
		if (zones.nonEmpty) zones.head else null
	}

	private def getContentPlayerBindings(zones: Seq[Zone]): ListBuffer[ContentPlayerBinding] = {
		players.filter(mediaPlayer => zones.contains(mediaPlayer.zone))
	}

	def play(media: ContentPlayerMediaContainer, withFadeIn: Boolean): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.play(media, withFadeIn))
	}

	def resume(media: ContentPlayerMediaContainer, withFadeIn: Boolean): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.resume(media, withFadeIn))
	}

	def pause(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.pause(media))
	}

	def stop(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.stop(media))
	}

	def clearHold(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.clearHold())
	}

	def setFadeValue(zones: Seq[Zone], value: Double): Unit = {
		getContentPlayerBindings(zones).foreach(player => player.setFadeValue(value))
	}

	def highlight(zone: Zone, on: Boolean): Unit = {
		if (getContentPlayerBinding(zone) == null) {
			return
		}
		getContentPlayerBinding(zone).highlight(on)
	}
}
