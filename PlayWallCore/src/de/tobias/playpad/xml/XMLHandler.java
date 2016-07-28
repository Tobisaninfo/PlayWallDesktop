package de.tobias.playpad.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Util Methods zum Laden von XML Files.
 * 
 * @author tobias
 * 
 * @since 5.0.1
 */
public class XMLHandler<T> {

	private Element rootElement;

	/**
	 * Lädt ein XML Dokument und speichert sich den RootNode.
	 * 
	 * @param path
	 *            Path zu XML Datei
	 * @throws DocumentException
	 *             Fehler in der XML Datei
	 * @throws IOException
	 *             IO Fehler (Bsp. Datei nicht vorhanden)
	 */
	public XMLHandler(Path path) throws DocumentException, IOException {
		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			rootElement = document.getRootElement();
		} else {
			throw new FileNotFoundException(path.toString());
		}
	}

	/**
	 * Erstellt einen neuen Handler mit einem RootElement.
	 * 
	 * @param rootElement
	 *            RootElement
	 */
	public XMLHandler(Element rootElement) {
		this.rootElement = rootElement;
	}

	/**
	 * Lädt ein Datenrecord auf einem Array von Daten
	 * 
	 * @param listElementTag
	 *            Datentype
	 * @param deserializer
	 *            Deserializer
	 * @return Liste von Daten
	 */
	public List<T> loadElements(String listElementTag, XMLDeserializer<T> deserializer) {
		List<T> list = new ArrayList<>();

		for (Object object : rootElement.elements(listElementTag)) {
			if (object instanceof Element) {
				Element element = (Element) object;
				T data = deserializer.loadElement(element);
				if (data != null) {
					list.add(data);
				}
			}
		}

		return list;
	}

	/**
	 * Speichert eine Liste von Objekten mittels Serializer.
	 * 
	 * @param listElementTag
	 *            Name der XML Element
	 * @param list
	 *            Liste der Daten
	 * @param serializer
	 *            Serializer
	 */
	public void saveElements(String listElementTag, Iterable<T> list, XMLSerializer<T> serializer) {
		for (T data : list) {
			Element element = rootElement.addElement(listElementTag);
			serializer.saveElement(element, data);
		}
	}

	/**
	 * Speichert eine XML in einem Path.
	 * 
	 * @param path
	 *            Path der Datei
	 * @param document
	 *            XML Document
	 * @throws UnsupportedEncodingException
	 *             Falsches Encoding
	 * @throws IOException
	 *             IO Fehler (Bsp. File nicht vorhanden)
	 */
	public static void save(Path path, Document document) throws UnsupportedEncodingException, IOException {
		if (Files.exists(path)) {
			XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
			writer.write(document);
			writer.close();
		} else {
			throw new FileNotFoundException(path.toString());
		}
	}
}
