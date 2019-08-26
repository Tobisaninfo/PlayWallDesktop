package de.tobias.playpad.plugin.api

import de.thecodelabs.logger.Logger
import de.thecodelabs.plugins.{Plugin, PluginDescriptor}
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import spark.Spark

class WebApiPlugin extends Plugin {
	override def startup(pluginDescriptor: PluginDescriptor): Unit = {
		Logger.debug("Enable Web API Plugin")

		Spark.port(9876)
		Spark.webSocket("/api", classOf[WebSocketHandler])
		Spark.get("/", (_, _) => "PlayWall API")
	}

	override def shutdown(): Unit = {
		Logger.debug("Disable Web API Plugin")

		Spark.stop()
	}
}
