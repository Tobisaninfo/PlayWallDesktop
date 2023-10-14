package de.tobias.playpad.initialize

import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.thecodelabs.utils.threading.Worker
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.viewcontroller.dialog.AutoUpdateDialog
import javafx.application.Platform
import javafx.scene.control.ButtonType

import java.io.IOException

class CheckUpdateTask extends PlayPadInitializeTask {
	override def name(): String = "Updates"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val globalSettings = instance.getGlobalSettings

		if (globalSettings.isAutoUpdate && !globalSettings.isIgnoreUpdate) {
			Worker.runLater(() => {
				val updateService = instance.getUpdateService
				updateService.fetchCurrentVersion()

				if (updateService.isUpdateAvailable) {
					Platform.runLater(() => {
						val autoUpdateDialog = new AutoUpdateDialog(updateService, null)
						autoUpdateDialog.showAndWait.ifPresent(response => {
							if (autoUpdateDialog.isSelected) {
								globalSettings.setIgnoreUpdate(true)
							}

							if (response == ButtonType.APPLY) {
								Logger.info("Install update")
								try {
									updateService.runVersionizerInstance(updateService.getAllLatestVersionEntries)
									System.exit(0)
								} catch {
									case e: IOException => Logger.error(e)
								}
							}
						})
					})
				}
			})
		}
	}
}
