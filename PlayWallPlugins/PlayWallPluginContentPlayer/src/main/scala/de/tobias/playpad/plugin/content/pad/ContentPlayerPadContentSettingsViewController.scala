package de.tobias.playpad.plugin.content.pad

import java.util

import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.{Zone, ZoneConfiguration}
import de.tobias.playpad.profile.Profile
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.scene.control.{Button, CheckBox}
import org.controlsfx.control.CheckListView

import scala.jdk.CollectionConverters._

class ContentPlayerPadContentSettingsViewController(val pad: Pad) extends PadSettingsTabViewController {

	@FXML
	var lastFrameCheckbox: CheckBox = _
	@FXML
	var zoneListView: CheckListView[Zone] = _

	@FXML
	var addAllZonesButton: Button = _
	@FXML
	var removeAllZonesButton: Button = _

	load("view", "ContentPadSettings", Localization.getBundle)

	override def init(): Unit = {
		val zoneConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ZoneConfiguration]
		zoneListView.getItems.addAll(zoneConfiguration.zones)

		addAllZonesButton.disableProperty().bind(Bindings.equal(Bindings.size(zoneListView.getCheckModel.getCheckedIndices), zoneListView.getItems.size()))
		removeAllZonesButton.disableProperty().bind(Bindings.isEmpty(zoneListView.getCheckModel.getCheckedIndices))
	}

	override def getName: String = Localization.getString("plugin.content.player.settings")

	override def loadSettings(pad: Pad): Unit = {
		val customSettings = pad.getPadSettings.getCustomSettings
		val lastFrameOption = customSettings.get(ContentPlayerPadContentFactory.lastFrame)
		if (lastFrameOption != null) {
			lastFrameCheckbox.setSelected(lastFrameOption.toString.toBoolean)
		}

		pad.getContent match {
			case content: ContentPlayerPadContent =>
				content.getSelectedZones.foreach(item => zoneListView.getCheckModel.check(item))
			case _ =>
		}
	}

	override def saveSettings(pad: Pad): Unit = {
		val customSettings = pad.getPadSettings.getCustomSettings
		customSettings.put(ContentPlayerPadContentFactory.lastFrame, lastFrameCheckbox.isSelected)

		val selectedZoneNames = zoneListView.getCheckModel.getCheckedItems.asScala.map(zone => zone.getName)
		customSettings.put(ContentPlayerPadContentFactory.zones, new util.ArrayList(selectedZoneNames.asJavaCollection))
	}

	@FXML
	def onAddAllZonesHandler(): Unit = {
		zoneListView.getCheckModel.checkAll()
	}

	@FXML
	def onRemoveAllZonesHandler(): Unit = {
		zoneListView.getCheckModel.clearChecks()
	}
}
