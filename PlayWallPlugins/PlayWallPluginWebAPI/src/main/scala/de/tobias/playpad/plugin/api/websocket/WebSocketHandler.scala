package de.tobias.playpad.plugin.api.websocket

import java.util.concurrent.ConcurrentLinkedQueue

import de.thecodelabs.logger.Logger
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations._

@WebSocket
class WebSocketHandler {

	private val sessions = new ConcurrentLinkedQueue[Session]

	@OnWebSocketConnect def connected(session: Session): Unit = {
		Logger.debug("WebSocket Client connected")
		sessions.add(session)
	}

	@OnWebSocketClose def closed(session: Session, statusCode: Int, reason: String): Unit = {
		Logger.debug("WebSocket Client disconnected")
		sessions.remove(session)
	}

	@OnWebSocketMessage def onRead(session: Session, message: String): Unit = {
		Logger.debug("Read from WebSocket: {0}", message)
	}

	@OnWebSocketError def onError(session: Session, error: Throwable): Unit = {
		Logger.warning(error.getMessage)
	}
}
