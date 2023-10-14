package de.tobias.playpad.initialize

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.project.ref.ProjectReferenceManager
import javafx.application.Platform

import java.util.UUID

class OpenLastDocumentTask extends PlayPadInitializeTask {
	override def name(): String = "Open Last Document"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		if (instance.getGlobalSettings.isOpenLastDocument) {
			val value = app.getUserDefaults.getData("project").asInstanceOf[UUID]
			if (value != null) {
				Platform.runLater(() => instance.openProject(ProjectReferenceManager.getProject(value), null))
				throw new PlayPadInitializeAbortException(this)
			}
		}
	}
}
