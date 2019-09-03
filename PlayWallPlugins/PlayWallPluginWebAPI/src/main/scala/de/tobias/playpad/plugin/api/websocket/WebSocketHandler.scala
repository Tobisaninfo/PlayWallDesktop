package de.tobias.playpad.plugin.api.websocket

import java.util.concurrent.ConcurrentLinkedQueue

import com.google.gson.{Gson, JsonObject}
import de.thecodelabs.logger.Logger
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.plugin.api.websocket.methods.{ProjectCurrentMethod, ProjectListMethod, ProjectOpenMethod}
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations._

@WebSocket
class WebSocketHandler {

	private val sessions = new ConcurrentLinkedQueue[Session]

	private val methods: Map[String, MethodExecutable] = Map(
		"project-list" -> new ProjectListMethod,
		"project-current" -> new ProjectCurrentMethod,
		"project-open" -> new ProjectOpenMethod
	)

	@OnWebSocketConnect def connected(session: Session): Unit = {
		Logger.debug("WebSocket Client connected")
		sessions.add(session)
	}

	@OnWebSocketClose def closed(session: Session, statusCode: Int, reason: String): Unit = {
		Logger.debug("WebSocket Client disconnected")
		sessions.remove(session)
	}

	@OnWebSocketMessage def onRead(session: Session, text: String): Unit = {
		Logger.debug("Read from WebSocket: {0}", text)

		val message: Message = WebSocketHandler.gson.fromJson(text, classOf[Message])
		val response = methods(message.`type`).execute(session, message)

		if (response != null) {
			WebSocketHandler.sendResponse(session, message, response)
		}
	}

	@OnWebSocketError def onError(session: Session, error: Throwable): Unit = {
		Logger.warning(error.getMessage)
	}
}

object WebSocketHandler {
	private val gson = new Gson()

	def sendResponse(session: Session, message: Message, response: JsonObject) = {
		response.addProperty("messageId", message.messageId)
		response.addProperty("type", message.`type`)
		session.getRemote.sendString(gson.toJson(response))
	}
}
