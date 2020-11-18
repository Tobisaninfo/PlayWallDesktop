package de.tobias.playpad.plugin.content.player

import java.util
import java.util.{List => JavaList}

import de.thecodelabs.storage.settings.annotation.{FilePath, Key}

@FilePath("players.json")
class PlayerInstanceConfiguration {
	@Key
	var instances: JavaList[PlayerInstance] = new util.ArrayList[PlayerInstance]()
}

class PlayerInstance {
	@Key
	var name: String = _
	@Key
	var x: Double = _
	@Key
	var y: Double = _
	@Key
	var width: Double = _
	@Key
	var height: Double = _
}
