package de.tobias.playpad.initialize
import java.util.UUID

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.project.loader.ProjectLoader
import de.tobias.playpad.project.ref.ProjectReferenceManager

class OpenLastDocumentTask extends PlayPadInitializeTask {
	override def name(): String = "Open Last Document"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		if (instance.getGlobalSettings.isOpenLastDocument) {
			val value = app.getUserDefaults.getData("project").asInstanceOf[UUID]
			if (value != null) {
				val loader = new ProjectLoader(ProjectReferenceManager.getProject(value))
				val project = loader.load
				instance.openProject(project, null)

				throw new PlayPadInitializeAbortException(this)
			}
		}
	}
}
