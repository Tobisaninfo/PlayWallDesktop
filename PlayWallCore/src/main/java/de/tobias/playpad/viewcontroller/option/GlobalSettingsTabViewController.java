package de.tobias.playpad.viewcontroller.option;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.settings.GlobalSettings;

public abstract class GlobalSettingsTabViewController extends NVC {

	/**
	 * Erstellt einen neuen Tab.
	 */
	public GlobalSettingsTabViewController() {

	}

	/**
	 * Lädt alle Einstellungen vom Model in die GUI.
	 *
	 * @param settings Aktuelles GlobalSettings
	 */
	public abstract void loadSettings(GlobalSettings settings);

	/**
	 * Speichert alle Änderungen in das Model.
	 *
	 * @param settings Aktuelles GlobalSettings
	 */
	public abstract void saveSettings(GlobalSettings settings);

	/**
	 * Gibt <code>true</code> zurück, wenn im Hauptprogramm etwas neu geladen werden muss.
	 *
	 * @return <code>true</code> Benötigt Reload
	 */
	public abstract boolean needReload();

	/**
	 * Prüft ob die eingetragen Einstellungen erlaubt sind. Bei falschen Eingaben können die Einstellungen nicht
	 * geschlossen werden.
	 *
	 * @return <code>true</code> Einstellungen erlaubt. <code>false</code> Einstellungen fehlerhaft.
	 */
	public abstract boolean validSettings();

	/**
	 * Gibt den Namen für den Tab zurück.
	 *
	 * @return Display Name des Tabs.
	 */
	public abstract String name();

}
