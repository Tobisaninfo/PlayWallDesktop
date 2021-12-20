package de.tobias.playpad.initialize

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.plugin.ModernPluginManager
import javafx.application.Platform
import org.controlsfx.dialog.ExceptionDialog

import java.io.IOException
import java.nio.file.{Path, Paths}

class PluginLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Plugins"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val parameter = instance.getParameters

		try // Load Plugin Path
			if (!parameter.getRaw.contains("noplugins")) {
				var pluginFolder: Path = null
				if (parameter.getNamed.containsKey("plugin")) {
					val pluginParam = parameter.getNamed.get("plugin")
					for (part <- pluginParam.split(":")) {
						pluginFolder = Paths.get(part)
						setupPlugins(pluginFolder)
					}
				}
				else {
					pluginFolder = ApplicationUtils.getApplication.getPath(PathType.LIBRARY)
					setupPlugins(pluginFolder)
				}
			}
		catch {
			case e: Exception =>
				Logger.error("Unable to load plugins")
				Logger.error(e)

				Platform.runLater(() => {
					val dialog = new ExceptionDialog(e)
					dialog.showAndWait()
				})
		}
	}

	@throws[IOException]
	private def setupPlugins(pluginPath: Path): Unit = {
		// Delete old plugins
		ModernPluginManager.getInstance.deletePlugins()
		// Load Plugins
		ModernPluginManager.getInstance.loadFile(pluginPath)
	}
}
