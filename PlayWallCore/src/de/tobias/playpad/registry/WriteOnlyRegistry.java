package de.tobias.playpad.registry;

import java.io.IOException;
import java.net.URL;

import org.dom4j.DocumentException;

import de.tobias.playpad.plugin.Module;

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
	 * @param module
	 *            Module zu dem diese Komponente gehört
	 * @throws IllegalArgumentException
	 *             Die Komponete gibt es bereits.
	 */
	public void registerComponent(C component, String id, Module module) throws IllegalArgumentException;

	/**
	 * Lädt aus einer XML Datei die Komponenten Deklaration und registriert diese automatisch.
	 * 
	 * @param url
	 *            URL zur Deklaration
	 * @param loader
	 *            ClassLoader
	 * @param module
	 *            Module zu dem diese Komponente gehört
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
	public void loadComponentsFromFile(URL url, ClassLoader loader, Module module)
			throws IOException, DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException;

	public default void loadComponentsFromFile(String name, Module module)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException {
		loadComponentsFromFile(getClass().getClassLoader().getResource(name), getClass().getClassLoader(), module);
	}

	public default void loadComponentsFromFile(String name, ClassLoader loader, Module module)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException {
		loadComponentsFromFile(loader.getResource(name), loader, module);
	}
}
