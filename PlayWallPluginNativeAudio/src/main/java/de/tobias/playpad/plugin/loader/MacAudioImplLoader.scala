package de.tobias.playpad.plugin.loader

import java.nio.file.Files

import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.audio.mac.NativeAudioMacHandlerFactory
import de.tobias.playpad.plugin.Module

/**
  * Created by tobias on 16.04.17.
  */
class MacAudioImplLoader extends AudioModuleLoader {

	private val ASSETS = "mac/"

	override def preInit(): Unit = {
		val app = ApplicationUtils.getApplication
		val resourceFolder = app.getPath(PathType.NATIVE_LIBRARY)
		if (Files.notExists(resourceFolder)) {
			Files.createDirectories(resourceFolder)
		}

		val (dest, copied) = copyResource(resourceFolder, ASSETS, "libNativeAudio.dylib")
		if (copied)
			System.load(dest.toString)
	}

	override def init(module: Module): Unit = {
		val registry = PlayPadPlugin.getRegistryCollection.getAudioHandlers
		val nativeMac = new NativeAudioMacHandlerFactory("NativeAudio")
		nativeMac.setName("NativeAudio")
		registry.registerComponent(nativeMac, module)
	}
}
