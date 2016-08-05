package de.tobias.playpad.xml;

import org.dom4j.Element;

/**
 * Schnittstelle um ein Object auf einem XML Tree zu deserialisieren.
 * 
 * @author tobias
 *
 * @param <T>
 *            Typ der Daten
 */
public interface XMLDeserializer<T> {

	/**
	 * Lädt ein Object auf XML Daten.
	 * 
	 * @param element
	 *            XML Objekt
	 * @return Daten aus dem XML
	 */
	public T loadElement(Element element);

}
