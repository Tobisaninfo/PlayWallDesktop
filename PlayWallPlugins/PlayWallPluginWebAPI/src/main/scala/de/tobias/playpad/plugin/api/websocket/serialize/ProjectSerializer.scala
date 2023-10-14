package de.tobias.playpad.plugin.api.websocket.serialize

import com.google.gson.{JsonArray, JsonObject}
import de.tobias.playpad.design.modern.ModernColor
import de.tobias.playpad.profile.Profile
import de.tobias.playpad.project.Project

object ProjectSerializer {

	def serializeProject(project: Project, profile: Profile): JsonObject = {
		val result = new JsonObject

		result.addProperty("id", project.getProjectReference.getUuid.toString)
		result.addProperty("name", project.getProjectReference.getName)

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

				val padDesign = new JsonObject
				if (pad.getPadSettings.getDesign.isEnableCustomBackgroundColor) {
					padDesign.add("normal", serializeDesign(pad.getPadSettings.getDesign.getBackgroundColor))
				}
				if (pad.getPadSettings.getDesign.isEnableCustomPlayColor) {
					padDesign.add("play", serializeDesign(pad.getPadSettings.getDesign.getPlayColor))
				}
				padObject.add("design", padDesign)

				padArray.add(padObject)
			})
			pageObject.add("pads", padArray)

			pageArray.add(pageObject)
		})
		result.add("pages", pageArray)

		val globalDesign = new JsonObject
		globalDesign.add("normal", serializeDesign(profile.getProfileSettings.getDesign.getBackgroundColor))
		globalDesign.add("play", serializeDesign(profile.getProfileSettings.getDesign.getPlayColor))
		result.add("design", globalDesign)

		val settings = new JsonObject
		settings.addProperty("columns", project.getSettings.getColumns)
		settings.addProperty("rows", project.getSettings.getRows)
		result.add("settings", settings)

		result
	}

	private def serializeDesign(color: ModernColor): JsonObject = {
		val json = new JsonObject

		color.getCurrentModernColor.ifPresent(c => {
			json.addProperty("hi", c.getColors.getHi)
			json.addProperty("low", c.getColors.getLow)
			json.addProperty("font", c.getColors.getFont)
			json.addProperty("button", c.getColors.getButton)
			json.addProperty("playbarBackground", c.getColors.getPlaybar.getBackground)
			json.addProperty("playbarTrack", c.getColors.getPlaybar.getTrack)
		})

		json
	}
}
