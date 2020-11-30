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

class PlayerInstanceSettingsViewController extends GlobalSettingsTabViewController with IGlobalReloadTask {

	@FXML
	var listView: ListView[PlayerInstance] = _

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

	load("view", "PlayerSettings", Localization.getBundle)

	override def init(): Unit = {
		listView.setCellFactory((_: ListView[PlayerInstance]) => new ListCell[PlayerInstance] {
			override def updateItem(item: PlayerInstance, empty: Boolean): Unit = {
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
				saveSettingsToPlayerInstance(oldValue)
				playerViewController.highlight(oldValue, on = false)
			}
			if (newValue != null) {
				showSettingsOfPlayerInstance(newValue)
				playerViewController.highlight(newValue, on = true)
			} else {
				clearTextFields()
			}
		})
	}

	private def saveSettingsToPlayerInstance(playerInstance: PlayerInstance): Unit = {
		playerInstance.setName(nameTextField.getText)
		playerInstance.x = xTextField.getText.toDouble
		playerInstance.y = yTextField.getText.toDouble
		playerInstance.width = widthTextField.getText.toDouble
		playerInstance.height = heightTextField.getText.toDouble
	}

	private def showSettingsOfPlayerInstance(playerInstance: PlayerInstance): Unit = {
		nameTextField.setText(playerInstance.getName)
		xTextField.setText(playerInstance.x.toInt.toString)
		yTextField.setText(playerInstance.y.toInt.toString)
		widthTextField.setText(playerInstance.width.toInt.toString)
		heightTextField.setText(playerInstance.height.toInt.toString)
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
		val newConfiguration = new PlayerInstance
		newConfiguration.setName(Localization.getString("plugin.content.player.settings.default_name"))

		ContentPluginMain.configuration.instances.add(newConfiguration)
		listView.getItems.add(newConfiguration)
	}

	@FXML
	def onRemoveHandle(): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			listView.getItems.remove(selectedItem)
			ContentPluginMain.configuration.instances.remove(selectedItem)
		}
	}

	override def loadSettings(settings: GlobalSettings): Unit = {
		listView.getItems.setAll(ContentPluginMain.configuration.instances)
	}

	override def saveSettings(settings: GlobalSettings): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToPlayerInstance(selectedItem)
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
