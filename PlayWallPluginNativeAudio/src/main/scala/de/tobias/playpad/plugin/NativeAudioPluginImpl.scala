package de.tobias.playpad.plugin

import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.util.OS
import de.thecodelabs.utils.util.OS.OSType
import de.thecodelabs.versionizer.config.Artifact
import de.tobias.playpad.plugin.loader.{MacAudioImplLoader, WindowsAudioImplLoader}

/**
  * Created by tobias on 16.04.17.
  */
class NativeAudioPluginImpl extends PlayPadPluginStub {

	private val NAME = "NativeAudioMac"
	private val IDENTIFIER = "de.tobias.playwall.plugin.nativeaudio"

	private var module: Module = _
	private var artifact: Artifact = _


	override def startup(): Unit = {
		module = new Module(NAME, IDENTIFIER)
		artifact = Storage.load(classOf[NativeAudioPluginImpl].getClassLoader.getResourceAsStream("build.json"), StorageTypes.JSON, classOf[Artifact])

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

	override def getArtifact: Artifact = artifact
}
