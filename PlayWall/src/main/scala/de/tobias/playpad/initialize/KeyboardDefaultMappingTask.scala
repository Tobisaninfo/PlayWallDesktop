package de.tobias.playpad.initialize

import de.thecodelabs.utils.application
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadImpl

class KeyboardDefaultMappingTask extends PlayPadInitializeTask {
	override def name(): String = "Keyboard Mapping"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val globalSettings = instance.getGlobalSettings

		globalSettings.getKeyCollection.loadDefaultFromFile("components/Keys.xml", Localization.getBundle)
	}
}
