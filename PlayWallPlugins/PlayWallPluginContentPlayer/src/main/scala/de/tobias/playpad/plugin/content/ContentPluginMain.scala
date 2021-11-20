package de.tobias.playpad.plugin.content

import de.thecodelabs.plugins.PluginDescriptor
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.player.ContentPlayerWindowController
import de.tobias.playpad.plugin.content.settings.{ContentPlayerPluginConfiguration, ZoneSettingsViewController}
import de.tobias.playpad.plugin.{Jni4NetBridgeInitializer, Module, PlayPadPluginStub, SettingsListener}
import de.tobias.playpad.profile.{Profile, ProfileListener}
import javafx.application.Platform
import nativecontentplayerwindows.ContentPlayerWindow

class ContentPluginMain extends PlayPadPluginStub with SettingsListener with ProfileListener {

	private var module: Module = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)

		Jni4NetBridgeInitializer.initialize()
		Jni4NetBridgeInitializer.loadDll(getClass.getClassLoader, "dlls/", "j4n", "NativeContentPlayerWindows.j4n.dll",
			"NativeContentPlayerWindows.j4n.dll", "NativeContentPlayerWindows.dll", "PVS.MediaPlayer.dll")

		ContentPluginMain.window = new ContentPlayerWindow()
		ContentPluginMain.window.SetSize(1440, 80)

		val localization = Localization.loadBundle("lang/base", getClass.getClassLoader)
		Localization.addResourceBundle(localization)

		PlayPadPlugin.getRegistries.getPadContents.loadComponentsFromFile("PadContent.xml", getClass.getClassLoader, module, localization)
		PlayPadPlugin.getInstance().addAdditionalProfileSettingsTab(() => new ZoneSettingsViewController)

		PlayPadPlugin.getInstance().addSettingsListener(this)
		Profile.registerListener(this)
	}

	override def shutdown(): Unit = {
		ContentPluginMain.playerViewController.window.Close()
	}

	override def getModule: Module = module

	override def onLoad(profile: Profile): Unit = {
		val path = profile.getRef.getCustomFilePath("Zones.json")
		val zoneConfiguration = Storage.load(path, StorageTypes.JSON, classOf[ContentPlayerPluginConfiguration])
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
		val pluginConfiguration = currentProfile.getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ContentPlayerPluginConfiguration]
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(pluginConfiguration))
	}
}

object ContentPluginMain {
	lazy val playerViewController: ContentPlayerWindowController = new ContentPlayerWindowController
	private var window: ContentPlayerWindow = _

	val zoneConfigurationKey = "ZoneConfiguration"
}