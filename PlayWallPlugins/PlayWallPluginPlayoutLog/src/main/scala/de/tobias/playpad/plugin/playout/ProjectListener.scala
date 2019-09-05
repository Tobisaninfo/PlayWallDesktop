package de.tobias.playpad.plugin.playout

import java.text.SimpleDateFormat

import de.thecodelabs.logger.Logger
import de.thecodelabs.storage.proxy.SettingsProxy
import de.tobias.playpad.log.LogSeasons
import de.tobias.playpad.plugin.GlobalListener
import de.tobias.playpad.plugin.playout.storage.PlayoutLogSettings
import de.tobias.playpad.project.Project

class ProjectListener extends GlobalListener {

	private val dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

	override def currentProjectDidChanged(newProject: Project): Unit = {
		val autoStart = SettingsProxy.getSettings(classOf[PlayoutLogSettings]).autoStartLogging()
		if (autoStart) {
			Logger.info("Start new PlayOutLog session")

			val settings = newProject.getSettings

			val logSeason = LogSeasons.createLogSeason(dateFormatter.format(System.currentTimeMillis), settings.getColumns, settings.getRows)
			logSeason.createProjectSnapshot(newProject)
		}
	}
}
