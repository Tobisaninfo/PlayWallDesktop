package de.tobias.playpad;

import java.net.URL;
import java.nio.file.Path;

/**
 * Diese Schnittstelle wird dafür verwendet, damit das Programm für Plugins nach Updates suchen kann und diese auch
 * durchführen kann.
 * 
 * @author tobias
 *
 * @since 5.0.0
 */

@Deprecated
public interface Updatable {

	/**
	 * Gibt die aktuelle Build Nummer zurück
	 * 
	 * @return build number
	 */
	public int getCurrentBuild();

	/**
	 * Gibt die altuelle Programmversion (lesbar) für den Nutzer zurück.
	 * 
	 * @return version string
	 */
	public String getCurrentVersion();

	/**
	 * Gibt die neue Buildnummer zurück.
	 * 
	 * @return build number
	 */
	public int getNewBuild();

	/**
	 * Gibt die neue Versionsnummer (lesbar) zurück.
	 * 
	 * @return version string
	 */
	public String getNewVersion();

	/**
	 * Lädt alle Informationen für Updates vom Server, sodass die Methoden bescheid wissen.
	 * 
	 * @return <code>true</code> Erfolgreich.
	 */
	public boolean checkUpdate();

	/**
	 * Gibt den Downloadlink für das Update zurück.
	 * 
	 * @return server url
	 */
	public URL getDownloadPath();

	/**
	 * Gibt den Lokalen Pfad zur Datei.
	 * 
	 * @return local path
	 */
	public Path getLocalPath();

	/**
	 * Gibt den Display Namen zurück. (lesbar)
	 * 
	 * @return name
	 */
	public String name();

}
