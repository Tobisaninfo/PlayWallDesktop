package de.tobias.playpad.plugin.content.settings

import java.util
import java.util.{UUID, List => JavaList}
import de.thecodelabs.storage.settings.annotation.{FilePath, Key}
import de.tobias.playpad.Displayable
import javafx.beans.property.{SimpleStringProperty, StringProperty}

@FilePath("players.json")
class ContentPlayerPluginConfiguration {
	@Key
	var screen: String = _
	@Key
	var zones: JavaList[Zone] = new util.ArrayList[Zone]()
	@Key
	var ffmpegExecutable: String = _
	@Key
	var ffprobeExecutable: String = _
}

class Zone extends Displayable {
	@Key
	var id: UUID = UUID.randomUUID()
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

	def move(x: Int, y: Int): Zone = {
		val zone = new Zone()
		zone.name = this.name
		zone.x = this.x + x
		zone.y = this.y + y
		zone.width = this.width
		zone.height = this.height
		zone
	}

	def toNative: nativecontentplayerwindows.Zone = new nativecontentplayerwindows.Zone(x.toInt, y.toInt, width.toInt, height.toInt)

	private val _displayProperty: StringProperty = new SimpleStringProperty(name)

	override def displayProperty(): StringProperty = {
		_displayProperty.set(name)
		_displayProperty
	}

	override def toString: String = name

	override def equals(other: Any): Boolean = other match {
		case that: Zone => id == that.id
		case _ => false
	}

	override def hashCode(): Int = {
		val state = Seq(name, x, y, width, height)
		state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
	}
}
