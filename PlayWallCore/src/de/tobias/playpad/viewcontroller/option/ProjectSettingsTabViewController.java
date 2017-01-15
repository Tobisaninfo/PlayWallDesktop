package de.tobias.playpad.viewcontroller.option;

import java.util.ResourceBundle;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.ui.ContentViewController;

/**
 * Abstracter Tab für Project Einstellungen.
 *
 * @author tobias
 * @see IProjectSettingsViewController
 * @since 5.1.0
 */
public abstract class ProjectSettingsTabViewController extends NVC {

	/**
	 * Erstellt einen neuen Tab.
	 */
	public ProjectSettingsTabViewController() {

	}

	/**
	 * Lädt alle Einstellungen vom Model in die GUI.
	 *
	 * @param settings Aktuelles Project Einstellungen
	 */
	public abstract void loadSettings(ProjectSettings settings);

	/**
	 * Speichert alle Änderungen in das Model.
	 *
	 * @param settings Aktuelles Project Einstellungen
	 */
	public abstract void saveSettings(ProjectSettings settings);

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
