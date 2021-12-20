package de.tobias.playpad.initialize

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.volume.{GlobalVolume, PadVolume, VolumeManager}

class VolumeInitializerTask extends PlayPadInitializeTask {
	override def name(): String = "Volume"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val volumeManager = VolumeManager.getInstance
		volumeManager.addFilter(new GlobalVolume)
		volumeManager.addFilter(new PadVolume)
	}
}
