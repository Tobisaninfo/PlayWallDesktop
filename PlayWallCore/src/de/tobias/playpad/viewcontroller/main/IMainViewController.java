package de.tobias.playpad.viewcontroller.main;

import java.util.List;

import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.utils.ui.NotificationHandler;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Schnittstelle für das Hautpfenster von PlayWall.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 *
 */
public interface IMainViewController extends NotificationHandler {

	/**
	 * Setzt die Grid Farbe.
	 * 
	 * @param color
	 *            Neue Farbe
	 */
	public void setGridColor(Color color);

	/**
	 * Gibt die Stage des ViewControllers zurück.
	 * 
	 * @return Stage
	 */
	public Stage getStage();

	/**
	 * Gibt den Root Node der View zurück.
	 * 
	 * @return root node
	 */
	public Parent getParent();

	/**
	 * Registriert ein KeyEvent Listener für das Fenster.
	 * 
	 * @param eventType
	 *            Event Type
	 * @param listener
	 *            Listener für Event
	 */
	public void registerKeyboardListener(EventType<KeyEvent> eventType, EventHandler<KeyEvent> listener);

	/**
	 * Aktualisiert den Title des Fenster.
	 */
	public void updateWindowTitle();

	/**
	 * Gibt den aktiven MenuToolbarController des Hauptfensters zurück. Dieser basiert auf dem aktuellen MainLayout.
	 * 
	 * @return Controller
	 */
	public MenuToolbarViewController getMenuToolbarController();

	/**
	 * Gibt den Bildschirm zurück, wo das Fenster aktiv ist,
	 * 
	 * @return Screen
	 */
	public Screen getScreen();

	/**
	 * Erstellt die Pad Views.
	 */
	public void createPadViews();

	/**
	 * Gibt die Nummer Aktuelle Seite zurück. (0, n)
	 * 
	 * @return Nummer der Seite
	 */
	public int getPage();

	/**
	 * Zeigt eine Seite. Sollte die Seite bereits offen sien, passiert nichts.
	 * 
	 * @param page
	 *            Page Number
	 */
	public void showPage(int page);

	/**
	 * Lädt die CSS Files neu.
	 */
	public void loadUserCss();

	/**
	 * Zeigt einen Hinweis, wenn eine Aktion versucht wird zu machen, wie während der Wiedergabe eines Pads nicht erlaubt ist. Die Methode
	 * delegiert an die Toolbar weiter.
	 * 
	 * @see MenuToolbarViewController#showLiveInfo(boolean)
	 */
	public void showLiveInfo();

	/**
	 * Gibt die PadViews zurück.
	 * 
	 * @return Liste der PadViews
	 */
	List<IPadViewV2> getPadViews();

	/**
	 * Gibt den MIDI Handler des Hauptfensters für die Kacheln zurück.
	 * 
	 * @return MIDI Handler
	 */
	public MidiListener getMidiHandler();

	/**
	 * Setzt das Globale Volume bei den Kacheln des aktuellen Projekts.
	 * 
	 * @param doubleValue
	 *            [0..1]
	 */
	public void setGlobalVolume(double doubleValue);

	/**
	 * Setzt das MainLayout des Hauptfensters.
	 * 
	 * @param mainLayoutConnect
	 *            Neues Layout
	 */
	public void setMainLayout(MainLayoutConnect mainLayoutConnect);

	/**
	 * Führt eine Aktion für das Hauptfenster aus. Beispielsweise MenuItem in MenuToolbarController hinzufügen. Zudem wird diese Aktion
	 * gespeichert und bei einem Layoutwechsel erneut ausgeführt.
	 * 
	 * @param runnable
	 *            Funktion
	 */
	public void performLayoutDependendAction(MainLayoutHandler runnable);

	/**
	 * Lädt die Tastenkombinationen für das Menü und co neu.
	 * 
	 * @param keys
	 *            Einstellungen der Key Bindings
	 */
	public void loadKeybinding(KeyCollection keys);
}
