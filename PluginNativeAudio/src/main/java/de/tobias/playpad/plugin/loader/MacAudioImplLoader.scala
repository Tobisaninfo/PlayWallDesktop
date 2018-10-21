package de.tobias.playpad.plugin.loader

import java.nio.file.Files

import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.audio.mac.NativeAudioMacHandlerFactory
import de.tobias.playpad.plugin.Module
import de.tobias.utils.application.ApplicationUtils
import de.tobias.utils.application.container.PathType

/**
  * Created by tobias on 16.04.17.
  */
class MacAudioImplLoader extends AudioImplLoader {

	private val ASSETS = "mac/"

	override def preInit(): Unit = {
		val app = ApplicationUtils.getApplication
		val resourceFolder = app.getPath(PathType.LIBRARY, "Native")
		if (Files.notExists(resourceFolder)) {
			Files.createDirectories(resourceFolder)
		}

		val dest = copyResource(resourceFolder, ASSETS, "libNativeAudio.dylib")
		System.load(dest.toString)
	}

	override def init(module: Module): Unit = {
		val registry = PlayPadPlugin.getRegistryCollection.getAudioHandlers
		val nativeMac = new NativeAudioMacHandlerFactory("NativeAudio")
		nativeMac.setName("NativeAudio")
		registry.registerComponent(nativeMac, module)
	}
}
