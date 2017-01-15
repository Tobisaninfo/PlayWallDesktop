package de.tobias.playpad;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import de.tobias.playpad.plugin.Module;
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
 * @since 5.0.0
 */
public interface PlayPad {

	/**
	 * Fügt einen Listener für das Hauptfenster hinzu.
	 *
	 * @param listener MainView Listener
	 * @since 2.0.0
	 */
	void addMainViewListener(WindowListener<IMainViewController> listener);

	/**
	 * Fügt einen Settings Listener hinzu.
	 *
	 * @param listener Settings Listener
	 * @since 2.0.0
	 */
	void addSettingsListener(SettingsListener listener);

	/**
	 * Entfernt einen Settings Listener.
	 *
	 * @param listener Settings Listener
	 * @since 2.0.0
	 */
	void removeSettingsListener(SettingsListener listener);

	/**
	 * Gibt alle SettingListener zurück.
	 *
	 * @return Settingslistener
	 */
	List<SettingsListener> getSettingsListener();

	/**
	 * Fügt ein PadListener zum System hinzu. Der Listener gilt für alle Pads.
	 *
	 * @param listener Listener
	 * @since 5.0.0
	 */
	void addPadListener(PadListener listener);

	/**
	 * Entfernt ein Pad Listener.
	 *
	 * @param listener Listener
	 */
	void removePadListener(PadListener listener);

	/**
	 * Gibt alle PadListener zurück.
	 *
	 * @return PadListener
	 */
	List<PadListener> getPadListener();

	/**
	 * Gibt eine Refernz auf das Hauptfenster zurück.
	 *
	 * @return Main ViewController
	 */
	IMainViewController getMainViewController();

	/**
	 * Gibt das Programm Icon zurück.
	 *
	 * @return Programmicon
	 */
	Optional<Image> getIcon();

	/**
	 * Beendet PlayWall.
	 */
	void shutdown();

	/**
	 * Lädt ein Plugin sofort ins System.
	 *
	 * @param uri Quelle des Plugin
	 */
	void loadPlugin(URI uri);

	/**
	 * Gibt die globalen Einstellungen zurück.
	 *
	 * @return Global Settings
	 */
	GlobalSettings getGlobalSettings();

	/**
	 * Gibt alle aktiven Module zurück.
	 *
	 * @return Module
	 */
	Collection<Module> getModules();
}
