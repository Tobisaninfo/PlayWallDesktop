package de.tobias.playpad.plugin.api

import de.thecodelabs.logger.Logger
import de.thecodelabs.plugins.{PluginArtifact, PluginDescriptor}
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.plugin.api.websocket.listener.{PadStatusListener, ProjectListener}
import de.tobias.playpad.plugin.{Module, PlayPadPluginStub}
import spark.Spark

class WebApiPlugin extends PlayPadPluginStub with PluginArtifact {

	private var module: Module = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)

		PlayPadPlugin.getInstance().addPadListener(new PadStatusListener)
		PlayPadPlugin.getInstance().addGlobalListener(new ProjectListener)

		Logger.debug("Enable Web API Plugin")

		Spark.port(9876)
		Spark.webSocket("/api", WebSocketHandler.instance)
		Spark.get("/", (_, _) => "PlayWall API")
	}

	override def shutdown(): Unit = {
		Logger.debug("Disable Web API Plugin")

		Spark.stop()
	}

	override def getModule: Module = module
}
