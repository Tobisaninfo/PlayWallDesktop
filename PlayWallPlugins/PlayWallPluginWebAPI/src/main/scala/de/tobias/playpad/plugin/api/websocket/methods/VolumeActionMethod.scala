package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.profile.Profile
import org.eclipse.jetty.websocket.api.Session

class VolumeActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val diff = message.payload.get("diff").getAsDouble

		val newValue = Profile.currentProfile.getProfileSettings.volumeProperty().get() + diff
		if (newValue >= 0 && newValue <= 1) {
			Profile.currentProfile.getProfileSettings.volumeProperty().set(newValue)
		}
		null
	}
}
