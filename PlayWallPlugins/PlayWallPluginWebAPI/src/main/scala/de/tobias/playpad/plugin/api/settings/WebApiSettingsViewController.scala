package de.tobias.playpad.plugin.api.settings

import de.thecodelabs.logger.Logger
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.api.WebApiPlugin
import de.tobias.playpad.settings.GlobalSettings
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control._

import java.nio.file.Files

class WebApiSettingsViewController(val webApiSettings: WebApiSettings) extends GlobalSettingsTabViewController {

	load("plugin/webapi/view", "WebApiSettings", Localization.getBundle)

	@FXML
	var activeCheckbox: CheckBox = _

	@FXML
	var portTextField: NumberTextField = _

	@FXML var remoteListView: ListView[WebApiRemoteSettings] = _
	@FXML var remoteAddButton: Button = _
	@FXML var remoteDeleteButton: Button = _
	@FXML var remoteNameTextField: TextField = _
	@FXML var remoteAddressTextField: TextField = _
	@FXML var remotePortTextField: NumberTextField = _

	override def init(): Unit = {
		remoteListView.getItems.setAll(webApiSettings.getRemoteSettings)
		remoteDeleteButton.disableProperty().bind(remoteListView.getSelectionModel.selectedItemProperty().isNull)

		remoteListView.setCellFactory((_: ListView[WebApiRemoteSettings]) => new ListCell[WebApiRemoteSettings] {
			override def updateItem(item: WebApiRemoteSettings, empty: Boolean): Unit = {
				super.updateItem(item, empty)
				if (!empty) {
					textProperty().bind(item.displayProperty())
				} else {
					textProperty().unbind()
					setText("")
				}
			}
		})
		remoteListView.getSelectionModel.selectedItemProperty().addListener((_, oldValue, newValue) => {
			if (oldValue != null) {
				saveSettingsToRemoteList(oldValue)
			}
			if (newValue != null) {
				showRemoteConfiguration(newValue)
			} else {
				clearTextFields()
			}
		})
	}

	private def saveSettingsToRemoteList(remote: WebApiRemoteSettings): Unit = {
		remote.setName(remoteNameTextField.getText)
		remote.setServerAddress(remoteAddressTextField.getText)
		remote.setPort(remotePortTextField.getText.toInt)
	}


	private def showRemoteConfiguration(remote: WebApiRemoteSettings): Unit = {
		remoteNameTextField.setText(remote.getName)
		remoteAddressTextField.setText(remote.getServerAddress)
		remotePortTextField.setText(remote.getPort.toString)
	}

	private def clearTextFields(): Unit = {
		remoteNameTextField.setText("")
		remoteAddressTextField.setText("")
		remotePortTextField.setText("")
	}

	@FXML def onRemoteAddButton(event: ActionEvent): Unit = {
		val remoteSettings = new WebApiRemoteSettings()
		remoteSettings.setName("Default")
		webApiSettings.getRemoteSettings.add(remoteSettings)

		remoteListView.getItems.add(remoteSettings)
	}

	@FXML def onRemoteDeleteButton(event: ActionEvent): Unit = {
		val selectedItem = remoteListView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			remoteListView.getItems.remove(selectedItem)
			webApiSettings.getRemoteSettings.remove(selectedItem)
		}
	}

	override def loadSettings(settings: GlobalSettings): Unit = {
		activeCheckbox.setSelected(webApiSettings.isEnabled)
		portTextField.setText(webApiSettings.getPort.toString)
	}

	override def saveSettings(settings: GlobalSettings): Unit = {
		webApiSettings.setEnabled(activeCheckbox.isSelected)
		webApiSettings.setPort(portTextField.getText.toInt)

		val selectedItem = remoteListView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToRemoteList(selectedItem)
		}

		saveToFile()
	}

	override def needReload(): Boolean = {
		false
	}

	override def validSettings(): Boolean = {
		true
	}

	override def name(): String = Localization.getString("webapi.settings")

	private def saveToFile(): Unit = {
		val settingsPath = WebApiPlugin.getWebApiSettingsPath
		if (Files.notExists(settingsPath)) {
			Files.createDirectories(settingsPath.getParent)
		}
		Storage.save(StorageTypes.JSON, webApiSettings)
		Logger.info(f"Save WebAPI settings to $settingsPath")
	}
}
