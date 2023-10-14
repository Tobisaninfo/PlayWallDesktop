package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import javafx.application.Platform
import org.eclipse.jetty.websocket.api.Session

class NavigateActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val mainViewController = PlayPadPlugin.getInstance.getMainViewController

		message.payload.get("action").getAsString match {
			case "PREVIOUS" =>
				Platform.runLater(() => mainViewController.showPage(mainViewController.getPage - 1))
			case "NEXT" =>
				Platform.runLater(() => mainViewController.showPage(mainViewController.getPage + 1))
			case _ =>
		}
		null
	}
}
