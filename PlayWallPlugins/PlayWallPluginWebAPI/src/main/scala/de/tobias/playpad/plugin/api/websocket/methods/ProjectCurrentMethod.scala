package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.plugin.api.websocket.serialize.ProjectSerializer
import org.eclipse.jetty.websocket.api.Session

class ProjectCurrentMethod extends MethodExecutable {
	override def execute(session: Session, message: Message): JsonObject = {
		val currentProject = PlayPadPlugin.getInstance().getCurrentProject

		ProjectSerializer.serializeProject(currentProject)
	}
}
