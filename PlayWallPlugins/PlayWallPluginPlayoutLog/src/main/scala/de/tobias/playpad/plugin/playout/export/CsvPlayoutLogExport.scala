package de.tobias.playpad.plugin.playout.export

import java.util.{Date, UUID}

import com.fasterxml.jackson.annotation.{JsonFormat, JsonIgnore, JsonProperty, JsonPropertyOrder}
import de.tobias.playpad.plugin.playout.log.LogSeason

import scala.jdk.CollectionConverters._

object CsvPlayoutLogExport {

	@JsonPropertyOrder(value = Array("name", "count", "seasonCount", "firstTime", "lastTime"))
	class CsvColumn
	(
		@JsonIgnore
		var id: UUID,
		@JsonProperty("Name")
		var name: String,
		@JsonProperty("Zaehler")
		var count: Int,
		@JsonProperty("Sessions")
		var seasonCount: Int,
		@JsonProperty("Erstes Datum")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
		var firstTime: Date,
		@JsonProperty("Letztes Datum")
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
		var lastTime: Date
	) {
	}

	def export(sessions: Array[LogSeason]): Array[CsvColumn] = {
		val items = sessions
			.map(_.getLogItems.asScala)
			.flatten
			.distinctBy(_.getUuid)
			.map(entry => new CsvColumn(entry.getUuid, entry.getName, 0, 0, null, null))

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
			timeMapping.filter(i => i._1 == item.id).minByOption(_._2).map(_._2) match {
				case Some(value) => item.firstTime = new Date(value)
				case _ =>
			}

			timeMapping.filter(i => i._1 == item.id).maxByOption(_._2).map(_._2) match {
				case Some(value) => item.lastTime = new Date(value)
				case _ =>
			}
		})

		items.groupBy(_.name).map((keyValue: (String, Array[CsvColumn])) => {
			new CsvColumn(
				null,
				keyValue._1,
				keyValue._2.map(i => i.count).sum,
				keyValue._2.map(i => i.seasonCount).maxOption.getOrElse(0),
				keyValue._2.map(i => i.firstTime).filter(_ != null).minOption.orNull,
				keyValue._2.map(i => i.firstTime).filter(_ != null).minOption.orNull
			)
		}).toArray
	}
}
