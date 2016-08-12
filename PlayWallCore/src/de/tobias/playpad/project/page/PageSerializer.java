package de.tobias.playpad.project.page;

import org.dom4j.Element;

import de.tobias.playpad.xml.XMLDeserializer;
import de.tobias.playpad.xml.XMLSerializer;

public class PageSerializer implements XMLSerializer<Page>, XMLDeserializer<Page> {

	@Override
	public Page loadElement(Element element) {
		return null;
	}
	
	@Override
	public void saveElement(Element newElement, Page data) {
		
	}
}
