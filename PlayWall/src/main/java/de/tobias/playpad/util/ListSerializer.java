package de.tobias.playpad.util;

import de.thecodelabs.storage.settings.UserDefaults;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class ListSerializer implements UserDefaults.Serializer<ArrayList> {

	private static final String LIST_ITEM = "item";

	@Override
	public ArrayList get(Element element) {
		ArrayList<Object> list = new ArrayList<>();
		for (Element child : element.elements(LIST_ITEM)) {
			final Object o = UserDefaults.loadElement(child);
			list.add(o);
		}
		return list;
	}

	@Override
	public void set(Object o, Element element) {
		if (o instanceof List) {
			//noinspection unchecked
			final List<Object> list = (List<Object>) o;

			for (Object item : list) {
				final Element childElement = element.addElement(LIST_ITEM);
				UserDefaults.save(childElement, item, null);
			}
		}
	}
}
