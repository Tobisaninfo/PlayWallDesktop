package de.tobias.playpad.plugin.content.settings

import java.util
import java.util.{List => JavaList}

import de.thecodelabs.storage.settings.annotation.{FilePath, Key}
import de.tobias.playpad.Displayable
import javafx.beans.property.{SimpleStringProperty, StringProperty}

@FilePath("players.json")
class ZoneConfiguration {
	@Key
	var zones: JavaList[Zone] = new util.ArrayList[Zone]()
}

class Zone extends Displayable {
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

	def toNative: nativecontentplayerwindows.Zone = new nativecontentplayerwindows.Zone(x.toInt, y.toInt, width.toInt, height.toInt)

	private val _displayProperty: StringProperty = new SimpleStringProperty(name)

	override def displayProperty(): StringProperty = {
		_displayProperty.set(name)
		_displayProperty
	}

	override def toString: String = name
}
