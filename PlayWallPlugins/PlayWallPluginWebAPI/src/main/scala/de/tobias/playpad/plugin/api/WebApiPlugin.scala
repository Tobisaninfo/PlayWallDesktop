package de.tobias.playpad.plugin.api

import de.thecodelabs.logger.Logger
import de.thecodelabs.plugins.{Plugin, PluginDescriptor}
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.plugin.api.websocket.listener.PadStatusListener
import spark.Spark

class WebApiPlugin extends Plugin {
	override def startup(pluginDescriptor: PluginDescriptor): Unit = {
		PlayPadPlugin.getInstance().addPadListener(new PadStatusListener)

		Logger.debug("Enable Web API Plugin")

		Spark.port(9876)
		Spark.webSocket("/api", WebSocketHandler.instance)
		Spark.get("/", (_, _) => "PlayWall API")
	}

	override def shutdown(): Unit = {
		Logger.debug("Disable Web API Plugin")

		Spark.stop()
	}
}
