package de.tobias.playpad.plugin.loader

import java.nio.file.Files

import de.thecodelabs.utils.application.ApplicationUtils
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.audio.mac.NativeAudioMacHandlerFactory
import de.tobias.playpad.plugin.{Module, NativeAudioPathType}

/**
  * Created by tobias on 16.04.17.
  */
class MacAudioImplLoader extends AudioModuleLoader {

	private val ASSETS = "mac/"

	private var loaded: Boolean = false

	override def preInit(): Unit = {
		val app = ApplicationUtils.getApplication
		val resourceFolder = app.getPath(NativeAudioPathType.AUDIO)
		if (Files.notExists(resourceFolder)) {
			Files.createDirectories(resourceFolder)
		}

		if (!loaded) {
			val dest = copyResource(resourceFolder, ASSETS, "libNativeAudio.dylib")
			System.load(dest.toString)
			loaded = true
		}
	}

	override def init(module: Module): Unit = {
		val registry = PlayPadPlugin.getRegistries.getAudioHandlers
		val nativeMac = new NativeAudioMacHandlerFactory("NativeAudio")
		nativeMac.setName("NativeAudio")
		registry.registerComponent(nativeMac, module)
	}
}
