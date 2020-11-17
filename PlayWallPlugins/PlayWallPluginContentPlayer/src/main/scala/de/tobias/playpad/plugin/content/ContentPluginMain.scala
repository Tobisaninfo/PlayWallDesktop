package de.tobias.playpad.plugin.content

import de.thecodelabs.plugins.PluginDescriptor
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.content.player.PlayerViewController
import de.tobias.playpad.plugin.{Module, PlayPadPluginStub}
import javafx.application.Platform

class ContentPluginMain extends PlayPadPluginStub {

	private var module: Module = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)

		val localization = Localization.loadBundle("lang/base", getClass.getClassLoader)
		Localization.addResourceBundle(localization)

		PlayPadPlugin.getRegistries.getPadContents.loadComponentsFromFile("PadContent.xml", getClass.getClassLoader, module, localization)
		Platform.runLater(() => {
			ContentPluginMain.playerViewController = new PlayerViewController
			ContentPluginMain.playerViewController.showStage()
		})
	}

	override def shutdown(): Unit = {

	}

	override def getModule: Module = module
}

object ContentPluginMain {
	var playerViewController: PlayerViewController = _
}