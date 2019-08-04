package de.tobias.playpad.initialize

import java.util.UUID

import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.project.loader.ProjectLoader
import de.tobias.playpad.project.ref.ProjectReferenceManager
;

class ProjectParameterOpenTask extends PlayPadInitializeTask {
	override def name(): String = "Open"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		val parameter = instance.getParameters

		// Auto Open Project DEBUG
		if (!parameter.getRaw.isEmpty) {
			if (parameter.getNamed.containsKey("project")) {
				val uuid = UUID.fromString(parameter.getNamed.get("project"))
				val loader = new ProjectLoader(ProjectReferenceManager.getProject(uuid))
				val project = loader.load
				instance.openProject(project, null)

				throw new PlayPadInitializeAbortException(this)
			}
		}
	}
}
