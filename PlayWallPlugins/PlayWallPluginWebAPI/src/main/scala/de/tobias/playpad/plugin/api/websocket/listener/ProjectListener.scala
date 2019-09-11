package de.tobias.playpad.plugin.api.websocket.listener

import de.tobias.playpad.plugin.GlobalAdapter
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.plugin.api.websocket.serialize.ProjectSerializer
import de.tobias.playpad.profile.Profile
import de.tobias.playpad.project.Project

class ProjectListener extends GlobalAdapter {
	override def projectOpened(newProject: Project): Unit = {
		val jsonObject = ProjectSerializer.serializeProject(newProject, Profile.currentProfile())
		WebSocketHandler.instance.sendUpdate("project-changed", jsonObject)
	}
}
