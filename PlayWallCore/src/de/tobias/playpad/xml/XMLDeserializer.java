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
	 * @return
	 */
	public T loadElement(Element element);

}
