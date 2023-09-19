package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import javafx.application.Platform
import org.eclipse.jetty.websocket.api.Session

import java.util.UUID

class CartActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val padId = UUID.fromString(message.payload.get("pad").getAsString)

		val currentProject = PlayPadPlugin.getInstance().getCurrentProject
		val pad = currentProject.getPad(padId)

		Platform.runLater(() => {
			if (pad.isPlay) {
				pad.stop()
			} else {
				pad.play()
			}
		})

		null
	}
}
