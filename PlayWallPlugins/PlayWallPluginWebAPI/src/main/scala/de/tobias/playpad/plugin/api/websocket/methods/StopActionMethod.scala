package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.pad.PadStatus
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import org.eclipse.jetty.websocket.api.Session

class StopActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val project = PlayPadPlugin.getInstance.getCurrentProject
		project.getPads.stream().forEach(pad => {
			if ((pad.getStatus eq PadStatus.PLAY) || (pad.getStatus eq PadStatus.PAUSE)) pad.setStatus(PadStatus.STOP, true)
		})
		null
	}
}
