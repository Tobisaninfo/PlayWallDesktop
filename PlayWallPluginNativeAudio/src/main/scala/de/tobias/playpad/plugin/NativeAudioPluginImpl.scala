package de.tobias.playpad.plugin

import de.thecodelabs.plugins.PluginArtifact
import de.thecodelabs.utils.util.OS
import de.thecodelabs.utils.util.OS.OSType
import de.tobias.playpad.plugin.loader.{MacAudioImplLoader, WindowsAudioImplLoader}

/**
  * Created by tobias on 16.04.17.
  */
class NativeAudioPluginImpl extends PlayPadPluginStub with PluginArtifact {

	private val NAME = "NativeAudio"
	private val IDENTIFIER = "de.tobias.playwall.plugin.nativeaudio"

	private var module: Module = _


	override def startup(): Unit = {
		module = new Module(NAME, IDENTIFIER)
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
}
