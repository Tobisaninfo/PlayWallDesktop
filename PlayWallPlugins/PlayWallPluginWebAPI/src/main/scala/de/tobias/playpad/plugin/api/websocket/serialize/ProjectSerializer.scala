package de.tobias.playpad.plugin.api.websocket.serialize

import com.google.gson.{JsonArray, JsonObject}
import de.tobias.playpad.project.Project

object ProjectSerializer {

	def serializeProject(project: Project): JsonObject = {
		val result = new JsonObject

		result.addProperty("id", project.getProjectReference.getUuid.toString)
		result.addProperty("name", project.getProjectReference.getName)
		result.addProperty("columns", project.getSettings.getColumns)
		result.addProperty("rows", project.getSettings.getRows)

		val pageArray = new JsonArray()
		project.getPages.forEach(page => {
			val pageObject = new JsonObject

			pageObject.addProperty("id", page.getId.toString)
			pageObject.addProperty("name", page.getName)
			pageObject.addProperty("position", page.getPosition)

			val padArray = new JsonArray()
			page.getPads.forEach(pad => {
				val padObject = new JsonObject
				padObject.addProperty("id", pad.getUuid.toString)
				padObject.addProperty("name", pad.getName)
				padObject.addProperty("status", pad.getStatus.name)

				padObject.addProperty("position", pad.getPosition)
				padObject.addProperty("page", pad.getPage.getPosition)

				padArray.add(padObject)
			})
			pageObject.add("pads", padArray)

			pageArray.add(pageObject)
		})
		result.add("pages", pageArray)
		result
	}
}
