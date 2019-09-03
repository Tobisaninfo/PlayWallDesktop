package de.tobias.playpad.plugin.api.websocket.methods

import com.google.gson.{JsonArray, JsonObject}
import de.tobias.playpad.plugin.api.websocket.MethodExecutable
import de.tobias.playpad.plugin.api.websocket.message.Message
import de.tobias.playpad.project.ref.ProjectReferenceManager
import org.eclipse.jetty.websocket.api.Session

import scala.jdk.CollectionConverters._

class ProjectListMethod extends MethodExecutable {
	override def execute(session: Session, message: Message): JsonObject = {
		val projectList = new JsonArray()

		ProjectReferenceManager.getProjects.asScala
    		.map(project => {
				val jsonObject = new JsonObject()
				jsonObject.addProperty("id", project.getUuid.toString)
				jsonObject.addProperty("name", project.getName)
				jsonObject
			}).foreach((i: JsonObject) => projectList.add(i))

		val response = new JsonObject()
		response.add("projects", projectList)
		response
	}
}
