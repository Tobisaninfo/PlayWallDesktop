package de.tobias.playpad.registry;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;

import de.tobias.playpad.plugin.Module;

/**
 * Eine Schnittstelle um Komponenten zu registrieren, aber nicht auszulesen.
 * 
 * @author tobias
 *
 * @param <C>
 *            Item
 * 
 * @since 5.1.0
 */
public interface WriteOnlyRegistry<C> {

	/**
	 * Registriert eine Komponente zu einer ID.
	 * 
	 * @param component
	 *            Komponente
	 * @param module
	 *            Module zu dem diese Komponente gehört
	 * @throws IllegalArgumentException
	 *             Die Komponete gibt es bereits.
	 */
	void registerComponent(C component, Module module) throws IllegalArgumentException;

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
	void loadComponentsFromFile(URL url, ClassLoader loader, Module module, ResourceBundle resourceBundle)
			throws IOException, DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException;

	default void loadComponentsFromFile(String name, Module module, ResourceBundle resourceBundle)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException, NoSuchMethodException, InvocationTargetException {
		loadComponentsFromFile(getClass().getClassLoader().getResource(name), getClass().getClassLoader(), module, resourceBundle);
	}

	default void loadComponentsFromFile(String name, ClassLoader loader, Module module, ResourceBundle resourceBundle)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, DocumentException, NoSuchMethodException, InvocationTargetException {
		loadComponentsFromFile(loader.getResource(name), loader, module, resourceBundle);
	}
}
