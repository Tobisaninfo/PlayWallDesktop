package de.tobias.playpad.project;

import java.nio.file.Path;

/**
 * Delegate Methode vom Model zum Import eines Projektes (mit Profile und Medien optionmal)
 * 
 * @author tobias
 * 
 * @since 5.0.0
 */
public interface Importable {

	/**
	 * Wenn ein Profil bereits vorhanden ist, wird hier nach einem neuen Namen gefragt.
	 * 
	 * @param name
	 *            Alter name
	 * @return Neuer Name oder null (dann wird nichts importiert)
	 */
	public String replaceProfile(String name);

	/**
	 * Wenn ein Projekt bereits vorhanden ist, wird hier nach einem neuen Namen gefragt.
	 * 
	 * @param name
	 *            Alter name
	 * @return Neuer Name oder null (dann wird nichts importiert)
	 */
	public String replaceProject(String name);

	/**
	 * Frage nach dem Ordner für die Mediendateien.
	 * 
	 * @return Ordner zum Import.
	 */
	public Path mediaFolder();
}