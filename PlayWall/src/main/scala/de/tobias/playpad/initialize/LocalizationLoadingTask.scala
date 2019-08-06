package de.tobias.playpad.initialize

import de.thecodelabs.utils.application
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.{PlayPadImpl, PlayPadLocalizationDelegate}

class LocalizationLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Localization"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		Localization.setDelegate(new PlayPadLocalizationDelegate)
		Localization.load()
	}
}
