package de.tobias.playpad.plugin.content.settings

import de.thecodelabs.utils.ui.icon.{FontAwesomeType, FontIcon}
import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.content.ContentPluginMain
import de.tobias.playpad.plugin.content.settings.ContentPlayerSettingsViewController.SelectableContentScreen
import de.tobias.playpad.profile.{Profile, ProfileSettings}
import de.tobias.playpad.project.Project
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.viewcontroller.option.{IProfileReloadTask, ProfileSettingsTabViewController}
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.stage.FileChooser
import nativecontentplayerwindows.{ContentPlayerWindow, ContentScreen}

import java.awt.Desktop
import java.net.URI

class ContentPlayerSettingsViewController extends ProfileSettingsTabViewController with IProfileReloadTask {

	import javafx.fxml.FXML

	@FXML var ffmpegButton: Button = _
	@FXML var ffmpegTextField: TextField = _
	@FXML var ffprobeButton: Button = _
	@FXML var ffprobeTextField: TextField = _
	@FXML var ffmpegDownloadLink: Hyperlink = _

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

	load("view", "ContentPlayerSettingsTab", Localization.getBundle)

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

		ffmpegButton.setGraphic(new FontIcon(FontAwesomeType.FOLDER))
		ffprobeButton.setGraphic(new FontIcon(FontAwesomeType.FOLDER))

		ffmpegDownloadLink.setText(Localization.getString("plugin.content.player.settings.ffmpeg_link"))
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
	@FXML def onFfmpegHandle(event: ActionEvent): Unit = {
		val chooser = new FileChooser()
		val selected = chooser.showOpenDialog(getContainingWindow)
		if (selected != null) {
			ffmpegTextField.setText(selected.toPath.toAbsolutePath.toString)
		}
	}

	@FXML def onFfprobeHandle(event: ActionEvent): Unit = {
		val chooser = new FileChooser()
		val selected = chooser.showOpenDialog(getContainingWindow)
		if (selected != null) {
			ffprobeTextField.setText(selected.toPath.toAbsolutePath.toString)
		}
	}

	@FXML def  onFfmpegDownloadLink(event: ActionEvent): Unit = {
		Desktop.getDesktop.browse(URI.create(ffmpegDownloadLink.getText))
	}

	@FXML
	def onAddHandle(): Unit = {
		val newConfiguration = new Zone
		newConfiguration.setName(Localization.getString("plugin.content.player.settings.default_name"))

		ContentPlayerSettingsViewController.getZoneConfiguration.zones.add(newConfiguration)
		listView.getItems.add(newConfiguration)
	}

	@FXML
	def onRemoveHandle(): Unit = {
		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			listView.getItems.remove(selectedItem)
			ContentPlayerSettingsViewController.getZoneConfiguration.zones.remove(selectedItem)
		}
	}

	override def loadSettings(settings: Profile): Unit = {
		val configuration = ContentPlayerSettingsViewController.getZoneConfiguration

		listView.getItems.setAll(configuration.zones)

		val screens = ContentPlayerWindow.GetScreens
		val selectedScreen = configuration.screen
		screenComboBox.getSelectionModel.select(new SelectableContentScreen(screens
		  .find(screen => screen.getName == selectedScreen)
		  .getOrElse(screens.head)))

		ffmpegTextField.setText(configuration.ffmpegExecutable)
		ffprobeTextField.setText(configuration.ffprobeExecutable)
	}

	override def saveSettings(settings: Profile): Unit = {
		val configuration = ContentPlayerSettingsViewController.getZoneConfiguration

		val selectedItem = listView.getSelectionModel.getSelectedItem
		if (selectedItem != null) {
			saveSettingsToZone(selectedItem)
		}
		val selectedScreen = screenComboBox.getSelectionModel.getSelectedItem
		if (selectedScreen != null) {
			configuration.screen = selectedScreen.getName
		}

		configuration.ffmpegExecutable = ffmpegTextField.getText
		configuration.ffprobeExecutable = ffprobeTextField.getText
	}

	override def needReload(): Boolean = {
		true
	}

	override def validSettings(): Boolean = {
		true
	}

	override def name(): String = Localization.getString("plugin.content.player.settings")


	override def getTask(settings: ProfileSettings, project: Project, controller: IMainViewController): Runnable = () =>
		Platform.runLater(() => ContentPluginMain.playerViewController.configurePlayers(ContentPlayerSettingsViewController.getZoneConfiguration))

}

object ContentPlayerSettingsViewController {
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
