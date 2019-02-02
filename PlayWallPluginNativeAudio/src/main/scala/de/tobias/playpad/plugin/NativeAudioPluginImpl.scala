package de.tobias.playpad.plugin

import de.thecodelabs.utils.util.OS
import de.thecodelabs.utils.util.OS.OSType
import de.tobias.playpad.plugin.loader.{MacAudioImplLoader, WindowsAudioImplLoader}
import de.tobias.updater.client.Updatable

/**
  * Created by tobias on 16.04.17.
  */
class NativeAudioPluginImpl extends AdvancedPlugin {

	private val NAME = "NativeAudioMac"
	private val IDENTIFIER = "de.tobias.playwall.plugin.nativeaudio"
	private val currentBuild = 1
	private val currentVersion = "1.0"

	private var module: Module = _
	private var updatable: Updatable = _


	override def startup(): Unit = {
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

	override def shutdown(): Unit = {

	}

	override def getModule: Module = module

	override def getUpdatable: Updatable = updatable
}