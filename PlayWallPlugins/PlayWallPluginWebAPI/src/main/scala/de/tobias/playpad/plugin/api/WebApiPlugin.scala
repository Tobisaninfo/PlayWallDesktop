package de.tobias.playpad.plugin.api

import de.thecodelabs.logger.Logger
import de.thecodelabs.plugins.PluginDescriptor
import de.thecodelabs.plugins.versionizer.PluginArtifact
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.thecodelabs.utils.threading.Worker
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.api.{PlayPadClient, PlayPadClientImpl}
import de.tobias.playpad.plugin.api.settings.{WebApiRemoteSettings, WebApiSettings, WebApiSettingsViewController}
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.plugin.api.websocket.listener.{PadStatusListener, ProjectListener}
import de.tobias.playpad.plugin.{Module, PlayPadPluginStub}
import spark.{Request, Response, Spark}

import java.nio.file.{Files, Path}
import java.util

class WebApiPlugin extends PlayPadPluginStub with PluginArtifact {

	private var module: Module = _

	private var webApiSettings: WebApiSettings = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)
		Localization.addResourceBundle("plugin/webapi/lang/base", getClass.getClassLoader)

		PlayPadPlugin.getInstance().addPadListener(new PadStatusListener)
		PlayPadPlugin.getInstance().addGlobalListener(new ProjectListener)

		Logger.debug("Enable Web API Plugin")

		PlayPadPlugin.getRegistries.getTriggerItems.loadComponentsFromFile("plugin/webapi/Trigger.xml", getClass.getClassLoader, module, Localization.getBundle)

		val settingsPath = WebApiPlugin.getWebApiSettingsPath
		if (Files.exists(settingsPath)) {
			webApiSettings = Storage.load(StorageTypes.JSON, classOf[WebApiSettings])
		}
		if (webApiSettings == null) {
			webApiSettings = new WebApiSettings
		}

		if (webApiSettings.isEnabled) {
			Spark.port(webApiSettings.getPort)
			Spark.webSocket("/api", WebSocketHandler.instance)
			Spark.get("/", (_: Request, _: Response) => "PlayWall API")
			Logger.info(f"Start WebAPI on port ${webApiSettings.getPort}")
		}

		webApiSettings.getRemoteSettings.forEach(remote => {
			Worker.runLater(() => {
				try {
					val client = new PlayPadClientImpl(f"ws://${remote.getServerAddress}:${remote.getPort}/api")
					WebApiPlugin.connections.put(remote, client)
					client.connect(5)
					Logger.info(s"Connected to remote PlayWall: ${remote.getName}")
				} catch {
					case e: Exception => Logger.error(e)
				}
			})
		})

		PlayPadPlugin.getInstance().addGlobalSettingsTab(() => new WebApiSettingsViewController(webApiSettings))
	}

	override def shutdown(): Unit = {
		Spark.stop()
		Logger.debug("Disable Web API Plugin")
	}

	override def getModule: Module = module

}

object WebApiPlugin {
	def getWebApiSettingsPath: Path = ApplicationUtils.getApplication.getPath(PathType.CONFIGURATION, "webapi.json")

	var connections: util.Map[WebApiRemoteSettings, PlayPadClient] = new util.HashMap()
}