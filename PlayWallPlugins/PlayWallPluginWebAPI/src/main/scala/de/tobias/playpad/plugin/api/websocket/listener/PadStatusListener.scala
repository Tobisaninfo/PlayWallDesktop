package de.tobias.playpad.plugin.api.websocket.listener

import com.google.gson.JsonObject
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.plugin.PadListener
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler

class PadStatusListener extends PadListener {

	override def onStatusChange(pad: Pad, newValue: PadStatus): Unit = {
		val payload = new JsonObject

		payload.addProperty("pad", pad.getUuid.toString)
		payload.addProperty("status", newValue.name())

		WebSocketHandler.instance.sendUpdate("pad-status-changed", payload)
	}
}
