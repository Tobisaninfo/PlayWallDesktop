package de.tobias.playpad.plugin.content.settings

import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.settings.GlobalSettings
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.viewcontroller.option.{GlobalSettingsTabViewController, IGlobalReloadTask}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{Button, ListCell, ListView, TextField}

class ZoneSettingsViewController extends GlobalSettingsTabViewController with IGlobalReloadTask {

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

		ContentPluginMain.configuration.zones.add(newConfiguration)
		listView.getItems.add(newConfiguration)
	}

	@FXML
	def onRemoveHandle(): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			listView.getItems.remove(selectedItem)
			ContentPluginMain.configuration.zones.remove(selectedItem)
		}
	}

	override def loadSettings(settings: GlobalSettings): Unit = {
		listView.getItems.setAll(ContentPluginMain.configuration.zones)
	}

	override def saveSettings(settings: GlobalSettings): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToZone(selectedItem)
		}

		Storage.save(StorageTypes.JSON, ContentPluginMain.configuration)
	}

	override def needReload(): Boolean = {
		true
	}

	override def validSettings(): Boolean = {
		true
	}

	override def name(): String = Localization.getString("plugin.content.player.settings")

	override def getTask(settings: GlobalSettings, controller: IMainViewController): Runnable = () =>
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(ContentPluginMain.configuration))
}
