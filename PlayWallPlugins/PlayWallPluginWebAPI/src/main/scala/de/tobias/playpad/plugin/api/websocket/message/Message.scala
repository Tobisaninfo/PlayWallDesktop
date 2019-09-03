package de.tobias.playpad.plugin.api.websocket.message

import com.google.gson.JsonObject

class Message (var `type`: String, var messageId: String, var payload: JsonObject) {

	// gson only
	def this() {
		this("", "", null)
	}
}
