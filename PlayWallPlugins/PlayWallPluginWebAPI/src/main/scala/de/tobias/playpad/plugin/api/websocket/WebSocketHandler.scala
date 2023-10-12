package de.tobias.playpad.plugin.api.websocket

import com.google.gson.{Gson, JsonObject}
import de.thecodelabs.logger.Logger
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.plugin.api.websocket.methods._
import org.eclipse.jetty.websocket.api.annotations._
import org.eclipse.jetty.websocket.api.{CloseException, Session}

import java.util.concurrent.ConcurrentLinkedQueue

@WebSocket
class WebSocketHandler {

	private val sessions = new ConcurrentLinkedQueue[Session]

	private val methods: Map[String, MethodExecutable] = Map(
		"ping" -> new PingMethod,
		"project-list" -> new ProjectListMethod,
		"project-current" -> new ProjectCurrentMethod,
		"project-open" -> new ProjectOpenMethod,
		"pad-status-change" -> new PadStatusChangeMethod,
		"cart-action" -> new CartActionMethod,
		"page-action" -> new PageActionMethod,
		"navigate-action" -> new NavigateActionMethod,
		"stop-action" -> new StopActionMethod,
		"current-page-request" -> new CurrentPageRequestMethod
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
		if (!error.isInstanceOf[CloseException]) {
			Logger.error(error)
		}
	}

	def sendUpdate(message: String, jsonObject: JsonObject): Unit = {
		jsonObject.addProperty("updateType", message)
		val payload = WebSocketHandler.gson.toJson(jsonObject)

		Logger.debug("Write to WebSocket: {0}", payload)
		sessions.stream()
			.filter(session => session.isOpen)
			.forEach(session => session.getRemote.sendStringByFuture(payload))
	}
}

object WebSocketHandler {

	lazy val instance: WebSocketHandler = new WebSocketHandler

	private val gson = new Gson()

	private def sendResponse(session: Session, message: Message, response: JsonObject): Unit = {
		response.addProperty("messageId", message.messageId)
		response.addProperty("type", message.`type`)

		val payload = gson.toJson(response)
		Logger.debug("Write to WebSocket: {0}", payload)
		session.getRemote.sendString(payload)
	}
}
