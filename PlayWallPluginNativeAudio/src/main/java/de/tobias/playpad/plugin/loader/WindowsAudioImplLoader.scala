package de.tobias.playpad.plugin.loader

import java.nio.file.Files

import de.thecodelabs.utils.application.container.PathType
import de.thecodelabs.utils.application.{App, ApplicationUtils}
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.audio.windows.NativeAudioWinHandlerFactory
import de.tobias.playpad.plugin.Module
import net.sf.jni4net.Bridge

/**
  * Created by tobias on 16.04.17.
  */
class WindowsAudioImplLoader extends AudioImplLoader {

	private val ASSETS = "win/"

	override def preInit(): Unit = {
		val app: App = ApplicationUtils.getApplication
		val resourceFolder = app.getPath(PathType.LIBRARY, "Native")

		if (!app.isDebug) {
			if (Files.notExists(resourceFolder))
				Files.createDirectories(resourceFolder)
			copyResource(resourceFolder, ASSETS, "jni4net.j-0.8.8.0.jar")
			copyResource(resourceFolder, ASSETS, "jni4net.n-0.8.8.0.dll")
			copyResource(resourceFolder, ASSETS, "jni4net.n.w32.v40-0.8.8.0.dll")
			copyResource(resourceFolder, ASSETS, "jni4net.n.w64.v40-0.8.8.0.dll")
			copyResource(resourceFolder, ASSETS, "NativeAudio.dll")
			copyResource(resourceFolder, ASSETS, "NativeAudio.j4n.dll")
			copyResource(resourceFolder, ASSETS, "NativeAudio.j4n.jar")
			copyResource(resourceFolder, ASSETS, "NAudio.dll")
		}

		Bridge.setVerbose(true)
		Bridge.init()
		Bridge.LoadAndRegisterAssemblyFrom(resourceFolder.resolve("NativeAudio.j4n.dll").toFile)
	}

	override def init(module: Module): Unit = {
		val registry = PlayPadPlugin.getRegistryCollection.getAudioHandlers
		val nativeWin = new NativeAudioWinHandlerFactory("NativeAudio")
		nativeWin.setName("NativeAudio")
		registry.registerComponent(nativeWin, module)
	}
}
