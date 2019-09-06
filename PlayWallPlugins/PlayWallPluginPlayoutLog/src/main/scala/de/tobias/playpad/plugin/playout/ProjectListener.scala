package de.tobias.playpad.plugin.playout

import java.text.SimpleDateFormat

import de.thecodelabs.logger.Logger
import de.thecodelabs.storage.proxy.SettingsProxy
import de.tobias.playpad.log.LogSeasons
import de.tobias.playpad.plugin.GlobalAdapter
import de.tobias.playpad.plugin.playout.storage.PlayoutLogSettings
import de.tobias.playpad.project.Project

class ProjectListener extends GlobalAdapter {

	private val dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
	private val nameFormat = "%s (%s)"

	override def projectOpened(newProject: Project): Unit = {
		val autoStart = SettingsProxy.getSettings(classOf[PlayoutLogSettings]).autoStartLogging()
		if (autoStart) {
			Logger.info("Start new PlayOutLog session")

			val settings = newProject.getSettings

			val logName = String.format(nameFormat, dateFormatter.format(System.currentTimeMillis), newProject.getProjectReference.getName)
			val logSeason = LogSeasons.createLogSeason(logName, settings.getColumns, settings.getRows)
			logSeason.createProjectSnapshot(newProject)
		}
	}

	override def projectClosed(currentProject: Project): Unit = LogSeasons.stop()
}
