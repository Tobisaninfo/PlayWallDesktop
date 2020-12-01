package de.tobias.playpad.plugin.content

import de.thecodelabs.plugins.PluginDescriptor
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.player.ContentPlayerViewController
import de.tobias.playpad.plugin.content.settings.{ZoneConfiguration, ZoneSettingsViewController}
import de.tobias.playpad.plugin.{Module, PlayPadPluginStub, SettingsListener}
import de.tobias.playpad.profile.{Profile, ProfileListener}
import javafx.application.Platform

class ContentPluginMain extends PlayPadPluginStub with SettingsListener with ProfileListener {

	private var module: Module = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)

		val localization = Localization.loadBundle("lang/base", getClass.getClassLoader)
		Localization.addResourceBundle(localization)

		PlayPadPlugin.getRegistries.getPadContents.loadComponentsFromFile("PadContent.xml", getClass.getClassLoader, module, localization)
		PlayPadPlugin.getInstance().addAdditionalProfileSettingsTab(() => new ZoneSettingsViewController)

		PlayPadPlugin.getInstance().addSettingsListener(this)
		Profile.registerListener(this)
	}

	override def shutdown(): Unit = {

	}

	override def getModule: Module = module

	override def onLoad(profile: Profile): Unit = {
		val path = profile.getRef.getCustomFilePath("Zones.json")
		val zoneConfiguration = Storage.load(path, StorageTypes.JSON, classOf[ZoneConfiguration])
		profile.addCustomSettings(ContentPluginMain.zoneConfigurationKey, zoneConfiguration)
	}

	override def onSave(profile: Profile): Unit = {
		val path = profile.getRef.getCustomFilePath("Zones.json")
		val zoneConfigurationObject = profile.getCustomSettings(ContentPluginMain.zoneConfigurationKey)
		if (zoneConfigurationObject != null) {
			Storage.save(path, StorageTypes.JSON, zoneConfigurationObject)
		}
	}

	override def reloadSettings(oldProfile: Profile, currentProfile: Profile): Unit = {
		val zoneConfiguration = currentProfile.getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ZoneConfiguration]
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(zoneConfiguration))
	}
}

object ContentPluginMain {
	lazy val playerViewController: ContentPlayerViewController = new ContentPlayerViewController

	val zoneConfigurationKey = "ZoneConfiguration"
}