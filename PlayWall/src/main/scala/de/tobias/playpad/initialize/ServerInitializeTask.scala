package de.tobias.playpad.initialize

import com.neovisionaries.ws.client.WebSocketException
import de.thecodelabs.logger.Logger
import de.thecodelabs.utils.application
import de.tobias.playpad.server.{Session, SessionDelegate, SessionNotExistsException}
import de.tobias.playpad.{PlayPadImpl, PlayPadPlugin}

import java.io.IOException

class ServerInitializeTask(delegate: SessionDelegate) extends PlayPadInitializeTask {
	override def name(): String = "Server"

	override def run(app: application.App, instance: PlayPadImpl): Unit = {
		// Load Server session key
		var session = Session.load
		if (session == null) session = delegate.getSession
		instance.setSession(session)

		if (session == Session.EMPTY) { // Connect to Server
			return
		}

		val server = PlayPadPlugin.getServerHandler.getServer
		try server.connect(session.getKey)
		catch {
			case e@(_: IOException | _: WebSocketException) =>
				Logger.error(e)
			case _: SessionNotExistsException =>
		}
	}
}
