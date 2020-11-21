package de.tobias.playpad.plugin.content.settings

import java.util
import java.util.{List => JavaList}

import de.thecodelabs.storage.settings.annotation.{FilePath, Key}
import de.tobias.playpad.Displayable
import javafx.beans.property.{SimpleStringProperty, StringProperty}

@FilePath("players.json")
class PlayerInstanceConfiguration {
	@Key
	var instances: JavaList[PlayerInstance] = new util.ArrayList[PlayerInstance]()
}

class PlayerInstance extends Displayable {
	@Key
	private var name: String = _
	@Key
	var x: Double = _
	@Key
	var y: Double = _
	@Key
	var width: Double = _
	@Key
	var height: Double = _

	def getName: String = name

	def setName(name: String): Unit = {
		this.name = name
		_displayProperty.set(name)
	}

	private val _displayProperty: StringProperty = new SimpleStringProperty(name)

	override def displayProperty(): StringProperty = {
		_displayProperty.set(name)
		_displayProperty
	}


	override def toString: String = name
}
