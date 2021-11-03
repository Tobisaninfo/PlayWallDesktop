package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.{Zone, ZoneConfiguration}
import nativecontentplayerwindows.{ContentPlayer, ContentPlayerWindow}

import scala.collection.mutable.ListBuffer

class ContentPlayerWindowController {

	val window: ContentPlayerWindow = new ContentPlayerWindow()
	val players: ListBuffer[ContentPlayerBinding] = ListBuffer.empty

	def configurePlayers(configuration: ZoneConfiguration): Unit = {
		if (configuration.zones.isEmpty) {
			window.Close()
			return
		}

		players.clear()
		window.ClearContentPlayers()

		configuration.zones.forEach(zone => {
			val contentPlayer = new ContentPlayer(zone.toNative)
			window.AddContentPlayer(contentPlayer)

			players.addOne(new ContentPlayerBinding(contentPlayer, zone))
		})

		window.Show()

		import scala.jdk.CollectionConverters._
		val zones = configuration.zones.asScala
		val maxWidth = zones.map(player => player.x + player.width).max.toInt
		val maxHeight = zones.map(player => player.y + player.height).max.toInt

		window.SetLocation(0, 0)
		window.SetSize(maxWidth, maxHeight)
	}

	private def getContentPlayerBinding(zone: Zone): ContentPlayerBinding = {
		getContentPlayerBindings(List(zone)).head
	}

	private def getContentPlayerBindings(zones: Seq[Zone]): ListBuffer[ContentPlayerBinding] = {
		players.filter(mediaPlayer => zones.contains(mediaPlayer.zone))
	}

	def play(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.play(media))
	}

	def resume(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.resume(media))
	}

	def pause(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.pause(media))
	}

	def stop(media: ContentPlayerMediaContainer): Unit = {
		getContentPlayerBindings(media.content.getSelectedZones).foreach(player => player.stop(media))
	}

	def setFadeValue(zones: Seq[Zone], value: Double): Unit = {
		// TODO: Implement
	}

	def highlight(zone: Zone, on: Boolean): Unit = {
		if (getContentPlayerBinding(zone) != null) {
			return
		}
		getContentPlayerBinding(zone).highlight(on)
	}
}
