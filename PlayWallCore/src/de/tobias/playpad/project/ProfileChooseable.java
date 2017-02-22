package de.tobias.playpad.project;

import de.tobias.playpad.settings.Profile;

/**
 * Dieses Interface stellt Methoden für die Wahl des Profiles für ein Projekt bereit.
 * 
 * @author tobias
 *
 * @since 5.0.0
 */
@Deprecated
public interface ProfileChooseable {

	/**
	 * Wird von der API beim Projekt laden aufgerunfen, wenn das referenzierte Projekt nicht gefunden wurde.
	 * 
	 * @return Neues Profile
	 */
	Profile getUnkownProfile();
}