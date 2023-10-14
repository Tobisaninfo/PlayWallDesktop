package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import org.eclipse.jetty.websocket.api.Session

class CurrentPageRequestMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val response = new JsonObject()
		response.addProperty("page", PlayPadPlugin.getInstance().getMainViewController.getPage)
		response
	}
}
