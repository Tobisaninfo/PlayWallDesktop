package de.tobias.playpad.plugin.content.player

import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.pad.ContentPlayerMediaContainer
import de.tobias.playpad.plugin.content.settings.{Zone, ContentPlayerPluginConfiguration, ZoneSettingsViewController}
import nativecontentplayerwindows.{ContentPlayer, ContentPlayerWindow}

import scala.collection.mutable.ListBuffer

class ContentPlayerWindowController {

	var window: ContentPlayerWindow = _
	val players: ListBuffer[ContentPlayerBinding] = ListBuffer.empty

	def configurePlayers(configuration: ContentPlayerPluginConfiguration): Unit = {
		players.foreach(_.clear())
		players.clear()
		if (window != null) {
			window.Close()
		}

		window = new ContentPlayerWindow()
		window.SetIcon(PlayPadPlugin.getInstance.getIconData)
		window.Show()

		import scala.jdk.CollectionConverters._
		val zones = configuration.zones.asScala

		val minX = zones.map(player => player.x).min.toInt
		val minY = zones.map(player => player.y).min.toInt

		zones.foreach(zone => {
			val contentPlayer = new ContentPlayer(zone.move(-minX, -minY).toNative)
			window.AddContentPlayer(contentPlayer)

			players.addOne(new ContentPlayerBinding(contentPlayer, zone))
		})

		val maxWidth = zones.map(player => player.x + player.width - minX).max.toInt
		val maxHeight = zones.map(player => player.y + player.height - minY).max.toInt

		val screens = ContentPlayerWindow.GetScreens
		val selectedScreen = ZoneSettingsViewController.getZoneConfiguration.screen
		val screen = screens.find(screen => screen.getName == selectedScreen)
		  .getOrElse(screens.head)

		window.SetSize(maxWidth, maxHeight)
		window.SetScreen(screen)
		window.SetLocation(minX, minY)
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
