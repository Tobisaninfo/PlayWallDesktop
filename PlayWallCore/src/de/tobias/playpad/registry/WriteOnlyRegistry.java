package de.tobias.playpad.registry;

import java.io.IOException;
import java.net.URL;

import org.dom4j.DocumentException;

/**
 * Eine Schnittstelle um Komponenten zu registrieren, aber nicht auszulesen.
 * 
 * @author tobias
 *
 * @param <C>
 *            Component
 * 
 * @since 5.1.0
 */
public interface WriteOnlyRegistry<C> {

	/**
	 * Registriert eine Komponente zu einer ID.
	 * 
	 * @param component
	 *            Komponente
	 * @param id
	 *            ID
	 * @throws IllegalArgumentException
	 *             Die Komponete gibt es bereits.
	 */
	public void registerComponent(C component, String id) throws IllegalArgumentException;

	/**
	 * Lädt aus einer XML Datei die Komponenten Deklaration und registriert diese automatisch.
	 * 
	 * @param url
	 *            URL zur Deklaration
	 * @throws IOException
	 *             Fehler beim Laden der Datei.
	 * @throws DocumentException
	 *             Fehler beim Laden des XML Documents
	 * @throws ClassNotFoundException
	 *             Die Klasse wurde nicht gefunden
	 * @throws IllegalAccessException
	 *             Unerlaubte Sichtbarkeit
	 * @throws InstantiationException
	 *             Die Klasse konnte nicht instanziert werden
	 */
	public void loadComponentsFromFile(URL url, ClassLoader loader)
			throws IOException, DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException;

	public default void loadComponentsFromFile(String name)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException {
		loadComponentsFromFile(getClass().getClassLoader().getResource(name), getClass().getClassLoader());
	}

	public default void loadComponentsFromFile(String name, ClassLoader loader)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException {
		loadComponentsFromFile(loader.getResource(name), loader);
	}
}
