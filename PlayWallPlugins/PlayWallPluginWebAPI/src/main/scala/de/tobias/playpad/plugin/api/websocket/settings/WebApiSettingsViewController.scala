package de.tobias.playpad.plugin.api.websocket.settings

import java.nio.file.Files

import de.thecodelabs.logger.Logger
import de.thecodelabs.storage.settings.{Storage, StorageTypes}
import de.thecodelabs.utils.ui.scene.input.NumberTextField
import de.thecodelabs.utils.util.Localization
import de.tobias.playpad.plugin.api.WebApiPlugin
import de.tobias.playpad.settings.GlobalSettings
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController
import javafx.fxml.FXML
import javafx.scene.control.CheckBox

class WebApiSettingsViewController(val webApiSettings: WebApiSettings) extends GlobalSettingsTabViewController {

	load("plugin/webapi/view", "WebApiSettings", Localization.getBundle)

	@FXML
	var activeCheckbox: CheckBox = _

	@FXML
	var portTextField: NumberTextField = _

	/**
	 * Lädt alle Einstellungen vom Model in die GUI.
	 *
	 * @param settings Aktuelles GlobalSettings
	 */
	override def loadSettings(settings: GlobalSettings): Unit = {
		activeCheckbox.setSelected(webApiSettings.isEnabled)
		portTextField.setText(webApiSettings.getPort.toString)
	}

	/**
	 * Speichert alle Änderungen in das Model.
	 *
	 * @param settings Aktuelles GlobalSettings
	 */
	override def saveSettings(settings: GlobalSettings): Unit = {
		webApiSettings.setEnabled(activeCheckbox.isSelected)
		webApiSettings.setPort(portTextField.getText.toInt)

		saveToFile()
	}

	/**
	 * Gibt <code>true</code> zurück, wenn im Hauptprogramm etwas neu geladen werden muss.
	 *
	 * @return <code>true</code> Benötigt Reload
	 */
	override def needReload(): Boolean = {
		false
	}

	/**
	 * Prüft ob die eingetragen Einstellungen erlaubt sind. Bei falschen Eingaben können die Einstellungen nicht
	 * geschlossen werden.
	 *
	 * @return <code>true</code> Einstellungen erlaubt. <code>false</code> Einstellungen fehlerhaft.
	 */
	override def validSettings(): Boolean = {
		true
	}

	/**
	 * Gibt den Namen für den Tab zurück.
	 *
	 * @return Display Name des Tabs.
	 */
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
