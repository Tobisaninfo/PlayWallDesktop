package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.pad.PadStatus
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.project.page.PadIndex
import javafx.application.Platform
import org.eclipse.jetty.websocket.api.Session

import java.util.UUID

class CartActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val padId = if (message.payload.get("padId") != null) {
			UUID.fromString(message.payload.get("padId").getAsString)
		} else if (message.payload.get("padIndex") != null) {
			val padIndex = message.payload.get("padIndex").getAsInt
			val currentPage = PlayPadPlugin.getInstance().getMainViewController.getPage
			val pad = PlayPadPlugin.getInstance().getCurrentProject.getPad(new PadIndex(padIndex, currentPage))
			pad.getUuid
		} else {
			throw new IllegalArgumentException("Neither pad nor padIndex provided")
		}

		val currentProject = PlayPadPlugin.getInstance().getCurrentProject
		val pad = currentProject.getPad(padId)

		if (pad == null || pad.getStatus == PadStatus.EMPTY || pad.getStatus == PadStatus.ERROR) {
			return null
		}
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
