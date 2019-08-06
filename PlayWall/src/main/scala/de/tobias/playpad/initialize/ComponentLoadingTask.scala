package de.tobias.playpad.initialize

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.audio.JavaFXHandlerFactory
import de.tobias.playpad.{PlayPadImpl, PlayPadPlugin}

class ComponentLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Component"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val resourceBundle = Localization.getBundle
		val module = instance.getModule
		val registries = PlayPadPlugin.getRegistries

		try {
			registries.getActions.loadComponentsFromFile("components/Actions.xml", module, resourceBundle)
			registries.getAudioHandlers.loadComponentsFromFile("components/AudioHandler.xml", module, resourceBundle)
			registries.getDragModes.loadComponentsFromFile("components/DragMode.xml", module, resourceBundle)
			registries.getPadContents.loadComponentsFromFile("components/PadContent.xml", module, resourceBundle)
			registries.getTriggerItems.loadComponentsFromFile("components/Trigger.xml", module, resourceBundle)
			registries.getMainLayouts.loadComponentsFromFile("components/Layout.xml", module, resourceBundle)

			registries.getAudioHandlers.setDefaultID(classOf[JavaFXHandlerFactory])
		} catch {
			case e: Exception => Logger.error(e)
		}
	}
}
