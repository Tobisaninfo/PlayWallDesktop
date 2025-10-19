package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.JsonObject
import de.thecodelabs.utils.threading.Worker
import de.tobias.playpad.PlayPadPlugin
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.listener.ProfileApiListener
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.plugin.api.websocket.serialize.ProjectSerializer
import de.tobias.playpad.profile.Profile
import org.eclipse.jetty.websocket.api.Session

class ProjectCurrentMethod extends MethodExecutable {
	override def execute(session: Session, message: Message): JsonObject = {
		val currentProject = PlayPadPlugin.getInstance().getCurrentProject

		Worker.runLater(() =>  ProfileApiListener.sendCurrentValue())
		if (currentProject == null) {
			null
		} else {
			ProjectSerializer.serializeProject(currentProject, Profile.currentProfile)
		}
	}
}
