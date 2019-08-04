package de.tobias.playpad.initialize
import de.thecodelabs.utils.application
import de.thecodelabs.utils.application.container.PathType
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.settings.GlobalSettings

class KeyboardMappingLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Keyboard Mapping"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val globalSettingsPath = app.getPath(PathType.CONFIGURATION, GlobalSettings.FILE_NAME)
		val globalSettings = instance.getGlobalSettings

		globalSettings.getKeyCollection.loadDefaultFromFile("components/Keys.xml", Localization.getBundle)
		globalSettings.getKeyCollection.load(globalSettingsPath)
	}
}
