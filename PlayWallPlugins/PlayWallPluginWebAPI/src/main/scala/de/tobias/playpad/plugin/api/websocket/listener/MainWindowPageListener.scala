package de.tobias.playpad.plugin.api.websocket.listener

import com.google.gson.JsonObject
import de.tobias.playpad.plugin.MainWindowListener
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.viewcontroller.main.IMainViewController

class MainWindowPageListener extends MainWindowListener {

	override def onInit(t: IMainViewController): Unit = {
	}

	override def onCurrentPageChanged(newPage: Int): Unit = {
		val payload = new JsonObject
		payload.addProperty("newPage", newPage)

		WebSocketHandler.instance.sendUpdate("current-page-changed", payload)
	}
}
