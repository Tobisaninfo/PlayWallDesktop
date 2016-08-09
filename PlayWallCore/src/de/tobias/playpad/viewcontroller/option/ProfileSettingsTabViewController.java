package de.tobias.playpad.viewcontroller.option;

import java.util.ResourceBundle;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.ContentViewController;

/**
 * Abstract Tab für SettingsViewController.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
public abstract class ProfileSettingsTabViewController extends ContentViewController {

	/**
	 * Erstellt einen neuen Tab.
	 * 
	 * @param name
	 *            Name der FXML
	 * @param path
	 *            Pfad zu FXML (ohne Dateiname)
	 * @param localization
	 *            ResourceBundle oder null
	 */
	public ProfileSettingsTabViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	/**
	 * Lädt alle Einstellungen vom Model in die GUI.
	 * 
	 * @param profile
	 *            Aktuelles Profile
	 */
	public abstract void loadSettings(Profile profile);

	/**
	 * Speichert alle Änderungen in das Model.
	 * 
	 * @param profile
	 *            Aktuelles Profile
	 */
	public abstract void saveSettings(Profile profile);

	/**
	 * Gibt <code>true</code> zurück, wenn im Hauptprogramm etwas neu geladen werden muss. Der eigentliche Reload wird in
	 * {@link #reload(Profile, Project, IMainViewController)} ausgeführt.
	 * 
	 * @return <code>true</code> Benötigt Reload
	 */
	public abstract boolean needReload();

	/**
	 * Lädt gestimmte Einstellungen für die GUI neu.
	 * 
	 * @param profile
	 *            Aktuelles Profile
	 * @param project
	 *            Aktuelles Projekt
	 * @param controller
	 *            Main View Controller
	 */
	public void reload(Profile profile, Project project, IMainViewController controller) {}

	/**
	 * Prüft ob die eingetragen Einstellungen erlaubt sind. Bei falschen Eingaben können die Einstellungen nicht geschlossen werden.
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
