package de.tobias.playpad.plugin.content.settings

import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.ZoneSettingsViewController.SelectableContentScreen
import de.tobias.playpad.profile.{Profile, ProfileSettings}
import de.tobias.playpad.project.Project
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.viewcontroller.option.{IProfileReloadTask, ProfileSettingsTabViewController}
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control._
import nativecontentplayerwindows.{ContentPlayerWindow, ContentScreen}

class ZoneSettingsViewController extends ProfileSettingsTabViewController with IProfileReloadTask {

	@FXML
	var screenComboBox: ComboBox[SelectableContentScreen] = _
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

		val screens = ContentPlayerWindow.GetScreens
		screenComboBox.getItems.setAll(screens.map(screen => new SelectableContentScreen(screen)): _*)
		screenComboBox.setCellFactory((_: ListView[SelectableContentScreen]) => new ContentScreenCell())
		screenComboBox.setButtonCell(new ContentScreenCell())
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

		ZoneSettingsViewController.getZoneConfiguration.zones.add(newConfiguration)
		listView.getItems.add(newConfiguration)
	}

	@FXML
	def onRemoveHandle(): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			listView.getItems.remove(selectedItem)
			ZoneSettingsViewController.getZoneConfiguration.zones.remove(selectedItem)
		}
	}

	override def loadSettings(settings: Profile): Unit = {
		listView.getItems.setAll(ZoneSettingsViewController.getZoneConfiguration.zones)

		val screens = ContentPlayerWindow.GetScreens
		val selectedScreen = ZoneSettingsViewController.getZoneConfiguration.screen
		screenComboBox.getSelectionModel.select(new SelectableContentScreen(screens
		  .find(screen => screen.getName == selectedScreen)
		  .getOrElse(screens.head)))
	}

	override def saveSettings(settings: Profile): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToZone(selectedItem)
		}
		val selectedScreen = screenComboBox.getSelectionModel.getSelectedItem
		if (selectedScreen != null) {
			ZoneSettingsViewController.getZoneConfiguration.screen = selectedScreen.getName
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
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(ZoneSettingsViewController.getZoneConfiguration))

}

object ZoneSettingsViewController {
	private[settings] class SelectableContentScreen(val contentScreen: ContentScreen) {
		override def equals(obj: Any): Boolean = {
			if (!obj.isInstanceOf[SelectableContentScreen]) {
				return false
			}
			contentScreen.getName == obj.asInstanceOf[SelectableContentScreen].getName
		}

		def getName: String = contentScreen.getName
	}

	def getZoneConfiguration: ContentPlayerPluginConfiguration = Profile.currentProfile().getCustomSettings(ContentPluginMain.zoneConfigurationKey).asInstanceOf[ContentPlayerPluginConfiguration]
}
