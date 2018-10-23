package de.tobias.playpad.viewcontroller.option;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.profile.Profile;

/**
 * Abstract Tab für SettingsViewController.
 *
 * @author tobias
 * @since 5.0.0
 */
public abstract class ProfileSettingsTabViewController extends NVC {

	/**
	 * Erstellt einen neuen Tab.
	 */
	public ProfileSettingsTabViewController() {

	}

	/**
	 * Lädt alle Einstellungen vom Model in die GUI.
	 *
	 * @param profile Aktuelles Profile
	 */
	public abstract void loadSettings(Profile profile);

	/**
	 * Speichert alle Änderungen in das Model.
	 *
	 * @param profile Aktuelles Profile
	 */
	public abstract void saveSettings(Profile profile);

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
