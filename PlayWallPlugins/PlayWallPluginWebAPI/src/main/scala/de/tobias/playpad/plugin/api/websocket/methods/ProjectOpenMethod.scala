package de.tobias.playpad.plugin.api.websocket.methods

import java.util.UUID

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.project.ref.ProjectReferenceManager
import javafx.application.Platform
import org.eclipse.jetty.websocket.api.Session

class ProjectOpenMethod extends MethodExecutable {
	override def execute(session: Session, message: Message): JsonObject = {
		val requestedId = UUID.fromString(message.payload.get("id").getAsString)

		val reference = ProjectReferenceManager.getProject(requestedId)
		Platform.runLater(() => PlayPadPlugin.getInstance().openProject(reference, null))
		null
	}
}
