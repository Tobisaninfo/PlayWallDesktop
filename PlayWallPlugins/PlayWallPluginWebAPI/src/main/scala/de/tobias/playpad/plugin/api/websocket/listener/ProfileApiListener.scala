package de.tobias.playpad.plugin.api.websocket.listener

import com.google.gson.JsonObject
import de.tobias.playpad.plugin.api.websocket.WebSocketHandler
import de.tobias.playpad.profile.{Profile, ProfileListener}
import javafx.beans.value.{ChangeListener, ObservableValue}

class ProfileApiListener extends ProfileListener with ChangeListener[Number] {

	override def reloadSettings(oldProfile: Profile, currentProfile: Profile): Unit = {
		if (oldProfile != null) {
			oldProfile.getProfileSettings.volumeProperty().removeListener(this)
		}
		currentProfile.getProfileSettings.volumeProperty().addListener(this)
	}

	override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
		val payload = new JsonObject

		payload.addProperty("newValue", newValue.doubleValue())

		WebSocketHandler.instance.sendUpdate("volume-changed", payload)
	}
}
