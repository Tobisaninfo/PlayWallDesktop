package de.tobias.playpad.plugin.playout.export

import java.util.UUID

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import de.tobias.playpad.plugin.playout.log.LogSeason

import scala.jdk.CollectionConverters._

object CsvPlayoutLogExport {

	@JsonPropertyOrder(value = Array("name", "count", "seasonCount", "firstTime", "lastTime"))
	class CsvColumn
	(
		var id: UUID,
		var name: String,
		var count: Int,
		var seasonCount: Int,
		var firstTime: Long,
		var lastTime: Long
	) {
	}

	def export(sessions: Array[LogSeason]): Array[CsvColumn] = {
		val items = sessions
			.map(_.getLogItems.asScala)
			.flatten
			.distinctBy(_.getUuid)
			.map(entry => new CsvColumn(entry.getUuid, entry.getName, 0, 0, 0, 0))

		sessions
			.map(_.getLogItems.asScala)
			.flatten
			.foreach(item => {
				val entry = items.filter(i => i.id == item.getUuid).head
				entry.count = entry.count + item.getPlayOutItems.size()
			})

		sessions
			.map(_.getLogItems.asScala.distinctBy(_.getUuid))
			.flatten
			.foreach(item => {
				val entry = items.filter(i => i.id == item.getUuid).head
				entry.seasonCount = entry.seasonCount + 1
			})

		val timeMapping = sessions
			.map(_.getLogItems.asScala)
			.flatten
			.map(i => i.getPlayOutItems.asScala)
			.flatten
			.map(i => i.getPathUuid -> i.getTime)

		items.foreach(item => {
			val min: Long = timeMapping.filter(i => i._1 == item.id).minByOption(_._2).map(_._2).getOrElse(0)
			val max: Long = timeMapping.filter(i => i._1 == item.id).maxByOption(_._2).map(_._2).getOrElse(0)

			item.firstTime = min
			item.lastTime = max
		})

		items
	}
}
