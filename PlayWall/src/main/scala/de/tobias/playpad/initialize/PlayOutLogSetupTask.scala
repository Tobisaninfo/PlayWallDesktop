package de.tobias.playpad.initialize

import java.nio.file.Path
import java.sql.SQLException

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.log.LogSeasons
import de.tobias.playpad.log.storage.SqlLiteLogSeasonStorageHandler

class PlayOutLogSetupTask extends PlayPadInitializeTask {
	override def name(): String = "PlayOutLog"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		try {
			val playOutLogPath = app.getPath(PathType.DOCUMENTS, "logging.db")
			LogSeasons.setStorageHandler(new SqlLiteLogSeasonStorageHandler(playOutLogPath))
			Logger.info("Setup LogSeasonStorageHandler in path: " + playOutLogPath)
		} catch {
			case e: SQLException => Logger.error("Cannot setup LogSeasonStorageHandler (" + e.getLocalizedMessage + ")")
		}
	}
}
