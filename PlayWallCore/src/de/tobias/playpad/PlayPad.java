package de.tobias.playpad;

import java.util.List;
import java.util.Optional;

import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.ISettingsViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.image.Image;
import net.xeoh.plugins.base.PluginManager;

public interface PlayPad {

	/**
	 * Fügt einen Listener für das Hauptfenster hinzu
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addMainViewListener(WindowListener<IMainViewController> listener);

	/**
	 * Entfernt ein registrierten Listener des Hauptfensters
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void removeMainViewListener(WindowListener<IMainViewController> listener);

	/**
	 * Fügt einen Listener zum Settings Fenster hinzu
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addSettingsViewListener(WindowListener<ISettingsViewController> listener);

	/**
	 * Entfernt einen Listener des Settings Fensters
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void removeSettingsViewListener(WindowListener<ISettingsViewController> listener);

	public List<WindowListener<ISettingsViewController>> getSettingsViewListener();

	/**
	 * Fügt einen Listener zum PadSettings Fenster hinzu
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addPadSettingsViewListener(WindowListener<IPadSettingsViewController> listener);

	/**
	 * Entfernt einen Listener vom PadSettings Fenster
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void removePadSettingsViewListener(WindowListener<IPadSettingsViewController> listener);

	public List<WindowListener<IPadSettingsViewController>> getPadSettingsViewListener();

	/**
	 * Fügt einen Settings Listener hinzu
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void addSettingsListener(SettingsListener listener);

	/**
	 * Entfernt einen Settings Listener
	 * 
	 * @param listener
	 * 
	 * @since 2.0.0
	 */
	public void removeSettingsListener(SettingsListener listener);

	public List<SettingsListener> getSettingsListener();

	public void addPadListener(PadListener listener);

	public void removePadListener(PadListener listener);

	public List<PadListener> getPadListener();

	public IMainViewController getMainViewController();

	public PluginManager getPluginManager();

	@Deprecated
	public String[] getProjectFiles();

	public Optional<Image> getIcon();

}
