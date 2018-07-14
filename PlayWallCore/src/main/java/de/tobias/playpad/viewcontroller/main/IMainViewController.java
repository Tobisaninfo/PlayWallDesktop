package de.tobias.playpad.viewcontroller.main;

import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.utils.nui.Alertable;
import de.tobias.utils.nui.NotificationHandler;
import de.tobias.utils.nui.scene.NotificationPane;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

/**
 * Schnittstelle für das Hautpfenster von PlayWall.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface IMainViewController extends NotificationHandler, Alertable {

	/**
	 * Setzt die Grid Farbe.
	 *
	 * @param color Neue Farbe
	 */
	void setGridColor(Color color);

	/**
	 * Gibt die Stage des ViewControllers zurück.
	 *
	 * @return Stage
	 */
	Stage getStage();

	/**
	 * Gibt den Root Node der View zurück.
	 *
	 * @return root node
	 */
	Parent getParent();

	/**
	 * Registriert ein KeyEvent Listener für das Fenster.
	 *
	 * @param eventType Event Type
	 * @param listener  Listener für Event
	 */
	void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener);

	/**
	 * Aktualisiert den Title des Fenster.
	 */
	void updateWindowTitle();

	/**
	 * Gibt den aktiven MenuToolbarController des Hauptfensters zurück. Dieser basiert auf dem aktuellen MainLayout.
	 *
	 * @return Controller
	 */
	MenuToolbarViewController getMenuToolbarController();

	/**
	 * Gibt den Bildschirm zurück, wo das Fenster aktiv ist,
	 *
	 * @return Screen
	 */
	Screen getScreen();

	/**
	 * Erstellt die Pad Views.
	 */
	void createPadViews();

	/**
	 * Gibt die Nummer Aktuelle Seite zurück. (0, n)
	 *
	 * @return Nummer der Seite
	 */
	int getPage();

	/**
	 * Zeigt eine Seite. Sollte die Seite bereits offen sien, passiert nichts.
	 *
	 * @param page Page Number
	 * @return <code>false</code> Seite gibt es nicht.
	 */
	boolean showPage(int page);

	/**
	 * Shows this page in the main view.
	 *
	 * @param page page
	 * @return success
	 */
	boolean showPage(Page page);

	/**
	 * Opens a project
	 *
	 * @param project project
	 */
	void openProject(Project project);

	/**
	 * Lädt die CSS Files neu.
	 */
	void loadUserCss();

	/**
	 * Gibt die PadViews zurück.
	 *
	 * @return Liste der PadViews
	 */
	List<IPadView> getPadViews();

	/**
	 * Gibt den MIDI Handler des Hauptfensters für die Kacheln zurück.
	 *
	 * @return MIDI Handler
	 */
	MidiListener getMidiHandler();

	/**
	 * Setzt das MainLayout des Hauptfensters.
	 *
	 * @param mainLayoutFactory Neues Layout
	 */
	void setMainLayout(MainLayoutFactory mainLayoutFactory);

	/**
	 * Führt eine Aktion für das Hauptfenster aus. Beispielsweise MenuItem in MenuToolbarController hinzufügen. Zudem wird diese Aktion
	 * gespeichert und bei einem Layoutwechsel erneut ausgeführt.
	 *
	 * @param runnable Funktion
	 */
	void performLayoutDependedAction(MainLayoutHandler runnable);

	/**
	 * Lädt die Tastenkombinationen für das Menü und co neu.
	 *
	 * @param keys Einstellungen der Key Bindings
	 */
	void loadKeybinding(KeyCollection keys);

	/**
	 * Gibt das NotificationPane zurück.
	 *
	 * @return NotificationPane
	 */
	NotificationPane getNotificationPane();

	<T extends Event> void addListenerForPads(EventHandler<? super T> handler, EventType<T> eventType);

	<T extends Event> void removeListenerForPads(EventHandler<? super T> handler, EventType<T> eventType);
}
