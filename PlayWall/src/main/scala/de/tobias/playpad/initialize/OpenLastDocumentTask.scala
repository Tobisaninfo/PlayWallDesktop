package de.tobias.playpad.initialize

import java.util.UUID

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.project.ref.ProjectReferenceManager

class OpenLastDocumentTask extends PlayPadInitializeTask {
	override def name(): String = "Open Last Document"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		if (instance.getGlobalSettings.isOpenLastDocument) {
			val value = app.getUserDefaults.getData("project").asInstanceOf[UUID]
			if (value != null) {
				instance.openProject(ProjectReferenceManager.getProject(value), null)
				throw new PlayPadInitializeAbortException(this)
			}
		}
	}
}
