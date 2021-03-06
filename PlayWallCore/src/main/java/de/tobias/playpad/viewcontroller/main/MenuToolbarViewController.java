package de.tobias.playpad.viewcontroller.main;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MenuType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCombination;

import java.util.ResourceBundle;

/**
 * Abstrakter ViewController für das Menu und die Toolbar des Hauptfenster. Jede Implementierung kann die GUI selbst bestimmen.
 *
 * @author tobias
 * @since 5.1.0
 */
public abstract class MenuToolbarViewController extends NVC {

	protected static final String CURRENT_PAGE_BUTTON = "current-page-button";

	/**
	 * Erstellt einen neuen ViewController ohne Localization
	 *
	 * @param path Name der FXML
	 * @param root Path zur FXML
	 */
	public MenuToolbarViewController(String path, String root) {
		load(root, path);
	}

	/**
	 * Erstellt einen eneun ViewController.
	 *
	 * @param name         Name der XML
	 * @param path         Path zur FXML
	 * @param localization Localization ResourceBundle
	 */
	public MenuToolbarViewController(String name, String path, ResourceBundle localization) {
		load(path, name, localization);
	}

	/**
	 * Wird von MainViwController aufgerufen, wenn die Buttons für die einzelnen Seiten neu erstellt werden müssen. Das ist der Fall beim
	 * laden eines Projektes und bei Änderungen an den Einstellungen.
	 */
	public abstract void initPageButtons();

	/*
	 * Handling Actions from MainViewController
	 */

	/**
	 * Mit dieser Methode werden mögliche Eingaben vom Nutzer geblocked (Beispiel: GUI Element disable). Diese Methode wird bei Änderung der
	 * Einstellung aufgerufen.
	 *
	 * @param looked true locked, false unlocked
	 * @see ProfileSettings#isLocked()
	 */
	public abstract void setLocked(boolean looked);

	/**
	 * Setzt das Menü für die Einstellung Always On Top.
	 *
	 * @param alwaysOnTopActive <code>true</code> Menu Aktiv
	 */
	public abstract void setAlwaysOnTopActive(boolean alwaysOnTopActive);

	/**
	 * Setzt das Menü für die Einstellung FullScreen.
	 *
	 * @param fullScreenActive <code>true</code> FullScreen
	 */
	public abstract void setFullScreenActive(boolean fullScreenActive);

	// Icons

	/**
	 * Fügt ein Node zur Toolbar hinzu.
	 *
	 * @param node node
	 */
	public abstract void addToolbarItem(Node node);

	/**
	 * Entfernt das Toolbar Item.
	 *
	 * @param node node
	 * @see MenuToolbarViewController#addToolbarItem(Node)
	 */
	public abstract void removeToolbarItem(Node node);

	// Menu Item

	/**
	 * Fügt ein MenuItem zu einem speziellen Menu hinzu.
	 *
	 * @param item MenuItem
	 * @param type Position
	 */
	public abstract void addMenuItem(MenuItem item, MenuType type);

	/**
	 * Entfernt ein spezielles MenuItem vom entsprechenden Menu.
	 *
	 * @param item MenuItem
	 */
	public abstract void removeMenuItem(MenuItem item);

	/*
	 * Getter
	 */

	/**
	 * Deinitialisiert den Controller.
	 */
	public abstract void deinit();

	/**
	 * Gibt den Global Volume Slider zurück. Jede Toolbar muss einen solchen für das Global Layout enthalten.
	 *
	 * @return Slider
	 */
	public abstract Slider getVolumeSlider();

	/**
	 * Set the count of problems.
	 *
	 * @param count count
	 */
	public abstract void setNotFoundNumber(int count);

	// Utils

	/**
	 * Hebt dem Page Button der Aktiv ist hervor. Gleichzeitig wird der vorherige Button nicht mehr Hervorgehebt.
	 *
	 * @param page Neue Seite
	 * @see IMainViewController#showPage(int)
	 */
	public abstract void highlightPageButton(int page);

	/**
	 * Lädt das Keyboard Binding.
	 *
	 * @param keys Einstellungen der Keybinding
	 */
	public abstract void loadKeybinding(KeyCollection keys);

	public void setKeyBindingForMenu(MenuItem menuItem, Key key) {
		if (key != null && key.getKey() != null && !key.getKeyCode().isEmpty()) {
			KeyCombination keyCode = KeyCombination.valueOf(key.getKeyCode());
			menuItem.setAccelerator(keyCode);
		}
	}

	/**
	 * Setzt eine Refernce des aktuellen Projectes auf den ViewController.
	 *
	 * @param project neues Project
	 */
	public abstract void setOpenProject(Project project);

	public abstract void initLayoutMenu();
}
