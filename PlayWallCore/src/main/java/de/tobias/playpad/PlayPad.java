package de.tobias.playpad;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.plugin.GlobalListener;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.ProjectReader.ProjectReaderDelegate.ProfileAbortException;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.image.Image;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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
	void addMainViewListener(MainWindowListener listener);

	List<MainWindowListener> getMainViewListeners();

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

	void addGlobalListener(GlobalListener globalListener);

	void removeGlobalListener(GlobalListener globalListener);

	List<GlobalListener> getGlobalListeners();

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
	Image getIcon();

	/**
	 * Beendet PlayWall.
	 */
	void shutdown();

	/**
	 * Gibt die globalen Einstellungen zurück.
	 *
	 * @return Global Settings
	 */
	GlobalSettings getGlobalSettings();

	/**
	 * Return the current project.
	 *
	 * @return project
	 */
	Project getCurrentProject();

	/**
	 * Open a project
	 *
	 * @param projectReference project reference
	 * @param onLoaded         on project loaded callback
	 * @throws IOException                                               io error
	 * @throws ProjectNotFoundException                                  Project to solve error not found
	 * @throws ProfileNotFoundException                                  Profile of project not found
	 * @throws DocumentException                                         XML Error
	 * @throws ProjectReader.ProjectReaderDelegate.ProfileAbortException Profile Choose aborted
	 */
	void openProject(ProjectReference projectReference, Consumer<NVC> onLoaded) throws ProjectNotFoundException, ProfileAbortException, ProfileNotFoundException, DocumentException, IOException;

	UpdateService getUpdateService();

}
