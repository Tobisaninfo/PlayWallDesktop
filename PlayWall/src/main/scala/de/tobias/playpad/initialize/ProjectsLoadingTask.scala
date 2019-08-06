package de.tobias.playpad.initialize
import de.thecodelabs.utils.application
import de.tobias.playpad.PlayPadImpl
import de.tobias.playpad.project.ref.ProjectReferenceManager

class ProjectsLoadingTask extends PlayPadInitializeTask {
	override def name(): String = "Projects"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		ProjectReferenceManager.loadProjects()
	}
}
