package de.tobias.playpad.plugin.api.websocket

import com.google.gson.JsonObject
import de.tobias.playpad.plugin.api.websocket.message.Message
import org.eclipse.jetty.websocket.api.Session

trait MethodExecutable {
	def execute(session: Session, message: Message): JsonObject
}
