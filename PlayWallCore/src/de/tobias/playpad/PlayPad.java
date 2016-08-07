package de.tobias.playpad;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.image.Image;

/**
 * Hauptfunktionen für Listener und zur Programmsteuerung für Plugins.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
public interface PlayPad {

	/**
	 * Fügt einen Listener für das Hauptfenster hinzu.
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addMainViewListener(WindowListener<IMainViewController> listener);

	/**
	 * Fügt einen Settings Listener hinzu.
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addSettingsListener(SettingsListener listener);

	/**
	 * Entfernt einen Settings Listener.
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void removeSettingsListener(SettingsListener listener);

	/**
	 * Gibt alle SettingListener zurück.
	 * 
	 * @return Settingslistener
	 */
	public List<SettingsListener> getSettingsListener();

	/**
	 * Fügt ein PadListener zum System hinzu. Der Listener gilt für alle Pads.
	 * 
	 * @param listener
	 *            Listener
	 * @since 5.0.0
	 */
	public void addPadListener(PadListener listener);

	/**
	 * Entfernt ein Pad Listener.
	 * 
	 * @param listener
	 *            Listener
	 */
	public void removePadListener(PadListener listener);

	/**
	 * Gibt alle PadListener zurück.
	 * 
	 * @return PadListener
	 * 
	 */
	public List<PadListener> getPadListener();

	/**
	 * Gibt eine Refernz auf das Hauptfenster zurück.
	 * 
	 * @return Main ViewController
	 */
	public IMainViewController getMainViewController();

	/**
	 * Gibt das Programm Icon zurück.
	 * 
	 * @return Programmicon
	 */
	public Optional<Image> getIcon();

	/**
	 * Beendet PlayWall.
	 */
	public void shutdown();

	/**
	 * Lädt ein Plugin sofort ins System.
	 * 
	 * @param uri
	 *            Quelle des Plugin
	 */
	public void loadPlugin(URI uri);

	/**
	 * Gibt die globalen Einstellungen zurück.
	 * 
	 * @return Global Settings
	 */
	public GlobalSettings getGlobalSettings();
}
