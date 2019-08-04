package de.tobias.playpad.initialize
import java.nio.file.Path

import de.thecodelabs.utils.application.App
import de.thecodelabs.utils.application.container.PathType
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.settings.GlobalSettings

class GlobalSettingsLoadingTask extends PlayPadInitializeTask {

	override def name(): String = "Global settings"

	override def run(app: App, instance: PlayPadImpl): Unit = {
		val globalSettingsPath = app.getPath(PathType.CONFIGURATION, GlobalSettings.FILE_NAME)
		val globalSettings = GlobalSettings.load(globalSettingsPath)

		instance.setGlobalSettings(globalSettings)
	}
}
