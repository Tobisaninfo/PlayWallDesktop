package de.tobias.playpad.plugin.api

import java.nio.file.{Files, Path}

import de.thecodelabs.logger.Logger
import de.thecodelabs.plugins.PluginDescriptor
import de.thecodelabs.plugins.versionizer.PluginArtifact
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.plugin.api.websocket.listener.{PadStatusListener, ProjectListener}
import de.tobias.playpad.plugin.api.websocket.settings.{WebApiSettings, WebApiSettingsViewController}
import de.tobias.playpad.plugin.{Module, PlayPadPluginStub}
import spark.{Request, Response, Spark}

class WebApiPlugin extends PlayPadPluginStub with PluginArtifact {

	private var module: Module = _

	private var webApiSettings: WebApiSettings = _

	override def startup(descriptor: PluginDescriptor): Unit = {
		module = new Module(descriptor.getName, descriptor.getArtifactId)

		PlayPadPlugin.getInstance().addPadListener(new PadStatusListener)
		PlayPadPlugin.getInstance().addGlobalListener(new ProjectListener)

		Logger.debug("Enable Web API Plugin")

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

		PlayPadPlugin.getInstance().addGlobalSettingsTab(() => new WebApiSettingsViewController(webApiSettings))
		Localization.addResourceBundle("plugin/webapi/lang/base", getClass.getClassLoader)
	}

	override def shutdown(): Unit = {
		Spark.stop()
		Logger.debug("Disable Web API Plugin")
	}

	override def getModule: Module = module

}

object WebApiPlugin {
	def getWebApiSettingsPath: Path = ApplicationUtils.getApplication.getPath(PathType.CONFIGURATION, "webapi.json")
}