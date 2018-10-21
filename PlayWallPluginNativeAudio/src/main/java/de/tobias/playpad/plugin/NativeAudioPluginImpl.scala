package de.tobias.playpad.plugin

import de.tobias.playpad.plugin.loader.{MacAudioImplLoader, WindowsAudioImplLoader}
import de.tobias.updater.client.Updatable
import de.tobias.utils.util.OS
import de.tobias.utils.util.OS.OSType
import net.xeoh.plugins.base.annotations.PluginImplementation
import net.xeoh.plugins.base.annotations.events.PluginLoaded

/**
  * Created by tobias on 16.04.17.
  */
@PluginImplementation
class NativeAudioPluginImpl extends NativeAudioPlugin {

	private val NAME = "NativeAudioMac"
	private val IDENTIFIER = "de.tobias.playwall.plugin.nativeaudio"
	private val currentBuild = 1
	private val currentVersion = "1.0"

	private var module: Module = _
	private var updatable: Updatable = _

	@PluginLoaded
	def onLoaded(plugin: NativeAudioPlugin): Unit = {
		module = new Module(NAME, IDENTIFIER)
		updatable = new StandardPluginUpdater(currentBuild, currentVersion, module)

		// Init Audio Implementation
		val loader = OS.getType match {
			case OSType.MacOSX => new MacAudioImplLoader
			case OSType.Windows => new WindowsAudioImplLoader
			case _ => null
		}

		if (loader != null) {
			loader.preInit()
			loader.init(module)
		}
	}

	override def getModule: Module = module

	override def getUpdatable: Updatable = updatable
}
