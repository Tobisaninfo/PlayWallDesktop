package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import javafx.application.Platform
import org.eclipse.jetty.websocket.api.Session

class PageActionMethod extends MethodExecutable {

	override def execute(session: Session, message: Message): JsonObject = {
		val project = PlayPadPlugin.getInstance.getCurrentProject
		val mainViewController = PlayPadPlugin.getInstance.getMainViewController
		val targetPage = message.payload.get("page").getAsInt

		if (targetPage < 0 || targetPage >= project.getPages.size) return null

		Platform.runLater(() => mainViewController.showPage(targetPage))
		return null
	}
}
