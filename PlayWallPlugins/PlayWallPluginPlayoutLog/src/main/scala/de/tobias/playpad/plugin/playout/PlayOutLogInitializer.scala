package de.tobias.playpad.plugin.playout

import java.sql.SQLException

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.plugin.playout.log.LogSeasons
import de.tobias.playpad.plugin.playout.storage.SqlLiteLogSeasonStorageHandler

object PlayOutLogInitializer {

	def init(): Unit = {
		val app = ApplicationUtils.getApplication

		try {
			val playOutLogPath = app.getPath(PathType.DOCUMENTS, "logging.db")
			LogSeasons.setStorageHandler(new SqlLiteLogSeasonStorageHandler(playOutLogPath))
			Logger.info("Setup LogSeasonStorageHandler in path: {0}", playOutLogPath)
		} catch {
			case e: SQLException => Logger.error("Cannot setup LogSeasonStorageHandler ({0})", e.getLocalizedMessage)
		}
	}
}
