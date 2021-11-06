package de.tobias.playpad.plugin.loader

import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.audio.windows.NativeAudioWinHandlerFactory
import de.tobias.playpad.plugin.{Jni4NetBridgeInitializer, Module}

/**
  * Created by tobias on 16.04.17.
  */
class WindowsAudioImplLoader extends AudioModuleLoader {

	override def preInit(): Unit = {
		Jni4NetBridgeInitializer.initialize()
		Jni4NetBridgeInitializer.loadDll(getClass.getClassLoader, "win/", "j4n", "NativeAudio.j4n.dll",
			"NativeAudio.j4n.dll", "NativeAudio.dll", "NAudio.dll")
	}

	override def init(module: Module): Unit = {
		val registry = PlayPadPlugin.getRegistries.getAudioHandlers
		val nativeWin = new NativeAudioWinHandlerFactory("NativeAudio")
		nativeWin.setName("NativeAudio")
		registry.registerComponent(nativeWin, module)
	}
}
