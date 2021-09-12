package de.tobias.playpad.plugin.content.settings

import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.profile.{Profile, ProfileSettings}
import de.tobias.playpad.project.Project
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.viewcontroller.option.{IProfileReloadTask, ProfileSettingsTabViewController}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{Button, ListCell, ListView, TextField}

class ZoneSettingsViewController extends ProfileSettingsTabViewController with IProfileReloadTask {

	@FXML
	var listView: ListView[Zone] = _

	@FXML
	var nameTextField: TextField = _
	@FXML
	var xTextField: NumberTextField = _
	@FXML
	var yTextField: NumberTextField = _
	@FXML
	var widthTextField: NumberTextField = _
	@FXML
	var heightTextField: NumberTextField = _

	@FXML
	var addButton: Button = _
	@FXML
	var removeButton: Button = _

	load("view", "ZoneSettings", Localization.getBundle)

	override def init(): Unit = {
		listView.setCellFactory((_: ListView[Zone]) => new ListCell[Zone] {
			override def updateItem(item: Zone, empty: Boolean): Unit = {
				super.updateItem(item, empty)
				if (!empty) {
					textProperty().bind(item.displayProperty())
				} else {
					textProperty().unbind()
					setText("")
				}
			}
		})
		listView.getSelectionModel.selectedItemProperty().addListener((_, oldValue, newValue) => {
			val playerViewController = ContentPluginMain.playerViewController

			if (oldValue != null) {
				saveSettingsToZone(oldValue)
				playerViewController.highlight(oldValue, on = false)
			}
			if (newValue != null) {
				showSettingsOfZone(newValue)
				playerViewController.highlight(newValue, on = true)
			} else {
				clearTextFields()
			}
		})
	}

	private def saveSettingsToZone(zone: Zone): Unit = {
		zone.setName(nameTextField.getText)
		zone.x = xTextField.getText.toDouble
		zone.y = yTextField.getText.toDouble
		zone.width = widthTextField.getText.toDouble
		zone.height = heightTextField.getText.toDouble
	}

	private def showSettingsOfZone(zone: Zone): Unit = {
		nameTextField.setText(zone.getName)
		xTextField.setText(zone.x.toInt.toString)
		yTextField.setText(zone.y.toInt.toString)
		widthTextField.setText(zone.width.toInt.toString)
		heightTextField.setText(zone.height.toInt.toString)
	}

	private def clearTextFields(): Unit = {
		nameTextField.setText("")
		xTextField.setText("")
		yTextField.setText("")
		widthTextField.setText("")
		heightTextField.setText("")
	}

	// Actions
	@FXML
	def onAddHandle(): Unit = {
		val newConfiguration = new Zone
		newConfiguration.setName(Localization.getString("plugin.content.player.settings.default_name"))

		getZoneConfiguration.zones.add(newConfiguration)
		listView.getItems.add(newConfiguration)
	}

	@FXML
	def onRemoveHandle(): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			listView.getItems.remove(selectedItem)
			getZoneConfiguration.zones.remove(selectedItem)
		}
	}

	override def loadSettings(settings: Profile): Unit = {
		listView.getItems.setAll(getZoneConfiguration.zones)
	}

	override def saveSettings(settings: Profile): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToZone(selectedItem)
		}
	}

	override def needReload(): Boolean = {
		true
	}

	override def validSettings(): Boolean = {
		true
	}

	override def name(): String = Localization.getString("plugin.content.player.settings")


	override def getTask(settings: ProfileSettings, project: Project, controller: IMainViewController): Runnable = () =>
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(getZoneConfiguration))

	private def getZoneConfiguration: ZoneConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ZoneConfiguration]
}
